package com.ming.shopping.beauty.service.service.impl;

import com.huotu.verification.IllegalVerificationCodeException;
import com.huotu.verification.service.VerificationCodeService;
import com.ming.shopping.beauty.service.aop.BusinessSafe;
import com.ming.shopping.beauty.service.config.ServiceConfig;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.login.User_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import me.jiangcai.wx.model.Gender;
import me.jiangcai.wx.standard.entity.StandardWeixinUser;
import me.jiangcai.wx.standard.repository.StandardWeixinUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author helloztt
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private RechargeCardService rechargeCardService;
    @Autowired
    private StandardWeixinUserRepository standardWeixinUserRepository;
    @Autowired
    private Environment env;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Login login = loginRepository.findByLoginName(username);
        if (login == null) {
            throw new UsernameNotFoundException(username);
        }
        return login;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    @BusinessSafe
    public Login getLogin(String openId, String mobile, String verifyCode
            , String familyName, Gender gender, String cardNo, Long guideUserId) {
        if (!env.acceptsProfiles(ServiceConfig.PROFILE_UNIT_TEST, "staging") && !StringUtils.isEmpty(verifyCode)) {
            try {
                verificationCodeService.verify(mobile, verifyCode, loginVerificationType());
            } catch (IllegalVerificationCodeException ex) {
                throw new ApiResultException(
                        ApiResult.withCodeAndMessage(ResultCodeEnum.THIRD_ERROR.getCode(), "验证码无效", null));
            }
        }
        if (!StringUtils.isEmpty(cardNo)) {
            rechargeCardService.verify(cardNo);
        }
        if (StringUtils.isEmpty(openId)) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.OPEN_ID_ERROR));
        }
        // 完成必要性检查，现在开始决定登录或者注册
        Login login = asWechat(openId);
        if (login != null && !StringUtils.isEmpty(login.getUsername())) {
            if (login.getUsername().equals(mobile)) {
                return login;
            } else {
                throw new ApiResultException(ApiResult.withError(ResultCodeEnum.USERNAME_ERROR));
            }
        }
        // 用户已存在但username为空
        //也许是初始化时未设置wechatUser的登录
        Login loginByName = loginRepository.findByLoginName(mobile);
        final StandardWeixinUser wechatUser = standardWeixinUserRepository.findByOpenId(openId);
        if (loginByName != null) {
            // TODO: 2018/2/7 单元测试还需要完善
            loginRepository.removeEmptyLogin(openId);
            loginByName.setWechatUser(wechatUser);
            return loginRepository.save(loginByName);
        }
        // login来自微信，
        return newUser(mobile, familyName, gender, cardNo, guideUserId, login, wechatUser);
    }

    @Override
    public Login newUser(String mobile, String familyName, Gender gender, String cardNo, Long guideUserId, Login login
            , StandardWeixinUser wechatUser) {
        //注册前校验手机号是否存在
        mobileVerify(mobile);

        if (StringUtils.isEmpty(familyName) || gender == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MESSAGE_NOT_FULL));
        }

        if (login == null) {
            login = new Login();
        } else {
            login = loginRepository.getOne(login.getId());
        }
        login.setLoginName(mobile);
        login.setWechatUser(wechatUser);
        login.setCreateTime(LocalDateTime.now());
        login.addLevel(ManageLevel.user);
        login = loginRepository.saveAndFlush(login);
        User user;
        if (login.getUser() == null) {
            user = new User();
            login.setUser(user);
            user.setLogin(login);
            if (guideUserId != null && guideUserId > 0) {
                //这里就不判断推荐人存不存在，可不可用了
                user.setGuideUser(loginRepository.findOne(guideUserId));
            }
        } else
            user = login.getUser();
        user.setId(login.getId());
        user.setFamilyName(familyName);
        user.setGender(gender);
        user = userRepository.saveAndFlush(user);
        if (!StringUtils.isEmpty(cardNo)) {
            //使用这张充值卡，如果不存在或者已经用过了，就抛出异常
            rechargeCardService.useCard(cardNo, login.getId());
            user.setCardNo(cardNo);
        }
        return login;
    }

    @Override
    public Login asWechat(String openId) {
        return loginRepository.findOne((root, query, cb)
                -> cb.equal(root.get(Login_.wechatUser).get("openId"), openId)
        );
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Login newEmpty(String openId) {
        Login login = asWechat(openId);
        if (login == null) {
            login = new Login();
            login.setWechatUser(standardWeixinUserRepository.findByOpenId(openId));
            loginRepository.save(login);
        }
        return login;
    }

    @Override
    public Login findOne(long id) throws ApiResultException {
        Login login = loginRepository.findOne(id);
        if (login == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_EXIST));
        }
        if (!login.isEnabled()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_ENABLE));
        }
        return login;
    }

    @Override
    public Login findOne(String mobile) throws ApiResultException {
        Login login = loginRepository.findByLoginName(mobile);
        if (login == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_EXIST));
        }
        if (!login.isEnabled()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_ENABLE));
        }
        return login;
    }

    @Override
    public void mobileVerify(String mobile) throws ApiResultException {
        if (loginRepository.countByLoginName(mobile) > 0) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MOBILE_EXIST));
        }
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void freezeOrEnable(long id, boolean enable) {
        if (loginRepository.updateLoginEnabled(id, enable) == 0) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_EXIST));
        }
    }

    @Override
    public void setGuidable(long id, boolean guidable) {
        Login login = loginRepository.findOne(id);
        login.setGuidable(guidable);
    }


    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void setManageLevel(long loginId, ManageLevel... manageLevel) {
        Login login = loginRepository.findOne(loginId);
        if (login.getLevelSet().contains(ManageLevel.root))
            throw new IllegalArgumentException("R-N-G");
        if (login.getLevelSet().stream().anyMatch(Login.merchantLevel::contains)) {
            throw new IllegalArgumentException("商户管理员无法被设置为平台管理员");
        }
        if (login.getLevelSet().contains(ManageLevel.user)) {
            login.getLevelSet().clear();
            login.addLevel(manageLevel);
            login.getLevelSet().add(ManageLevel.user);
            loginRepository.save(login);
        } else {
            login.getLevelSet().clear();
            login.addLevel(manageLevel);
            loginRepository.save(login);
        }
    }

    @Autowired
    private EntityManager entityManager;

    @Override
    public BigDecimal findBalance(long userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<User> root = cq.from(User.class);
        cq = cq
                .select(User.getCurrentBalanceExpr(root, cb))
                .where(cb.equal(root.get(User_.id), userId));

        try {
            BigDecimal rs = entityManager.createQuery(cq)
                    .getSingleResult();
            if (rs == null)
                return BigDecimal.ZERO;
            return rs;
        } catch (NoResultException ignored) {
            return BigDecimal.ZERO;
        }
    }
}
