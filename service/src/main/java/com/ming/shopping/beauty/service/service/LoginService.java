package com.ming.shopping.beauty.service.service;

import com.huotu.verification.VerificationType;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.utils.Constant;
import me.jiangcai.lib.notice.Content;
import me.jiangcai.wx.model.Gender;
import me.jiangcai.wx.standard.entity.StandardWeixinUser;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author helloztt
 */
public interface LoginService extends UserDetailsService {

    /**
     * 根据openId 查找用户，如果查到了就返回这个用户，如果没查到就创建用户
     * 创建用户，如果有卡密，那就激活这个用户
     *
     * @param openId      微信唯一标示
     * @param mobile      手机号
     * @param verifyCode  验证码
     * @param familyName  姓
     * @param gender      性别
     * @param cardNo      卡密
     * @param guideUserId 引导者
     * @return
     */
    @Transactional
    Login getLogin(String openId, String mobile, String verifyCode
            , String familyName, Gender gender, String cardNo, Long guideUserId);

    /**
     * @param mobile      手机号码
     * @param familyName  姓
     * @param gender      性别
     * @param cardNo      可选激活卡号
     * @param guideUserId 可选引导者
     * @param login       可选的已存在login
     * @param wechatUser  微信用户
     * @return 创建用户若用户不存在
     */
    @Transactional
    Login newUser(String mobile, String familyName, Gender gender, String cardNo, Long guideUserId, Login login
            , StandardWeixinUser wechatUser);

    /**
     * 根据openId 查找角色
     *
     * @param openId
     * @return
     */
    Login asWechat(String openId);

    Login newEmpty(String openId);

    /**
     * 查找角色，并校验角色是否可用，若不可用则抛出异常
     *
     * @param id
     * @return
     * @throws ApiResultException 校验失败返回结果
     */
    Login findOne(long id) throws ApiResultException;

    /**
     * 查找角色，并校验角色是否可用，若不可用则抛出异常
     *
     * @param mobile
     * @return
     * @throws ApiResultException
     */
    Login findOne(String mobile) throws ApiResultException;

    /**
     * 校验手机号是否已被注册，若被注册则抛出异常
     *
     * @param mobile
     * @throws ApiResultException
     */
    void mobileVerify(String mobile) throws ApiResultException;

    /**
     * 冻结或启用用户
     *
     * @param id
     * @param enable 是否启用
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void freezeOrEnable(long id, boolean enable);

    /**
     * 设置一个用户是否可推荐他人
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void setGuidable(long id , boolean guidable);
    /**
     * 用于登录的验证码
     *
     * @return
     */
    default VerificationType loginVerificationType() {
        return new VerificationType() {
            @Override
            public int id() {
                return 1;
            }

            @Override
            public boolean allowMultiple() {
                return true;
            }

            @Override
            public String message(String code) {
                return "短信验证码为：" + code + "；请勿泄露。";
            }

            @Override
            public Content generateContent(String code) {
                return Constant.generateCodeContent(this, code, "SMS_94310019");
            }
        };
    }

    /**
     * 给用户设置他的管理权限
     * @param loginId  被操作的用户
     * @param manageLevel 设置的等级
     */
    @Transactional
    void setManageLevel(long loginId, ManageLevel... manageLevel);

    /**
     * 查询用户余额
     * @param  userId
     * @return  余额
     */
    BigDecimal findBalance(long userId);
}
