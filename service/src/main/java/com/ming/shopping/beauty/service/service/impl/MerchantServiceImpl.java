package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author lxf
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private LoginService loginService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Merchant addMerchant(long loginId, Merchant merchant) throws ApiResultException {
        Login login = loginService.findOne(loginId);
        if (login.getLevelSet().stream().anyMatch(Login.rootLevel::contains)) {
            throw new IllegalArgumentException("平台管理员无法被设置为商户管理员");
        }
        if (login.getMerchant() != null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_MERCHANT_EXIST));
        }
        final Merchant newMerchant;
        if (merchant.getClass() == Merchant.class) {
            newMerchant = merchant;
        } else {
            newMerchant = new Merchant();
            newMerchant.fromRequest(merchant);
        }
        login.setMerchant(newMerchant);
        login.addLevel(ManageLevel.merchantRoot);
        newMerchant.setId(login.getId());
        newMerchant.setLogin(login);
        newMerchant.setCreateTime(LocalDateTime.now());
        return merchantRepository.save(newMerchant);
    }

    @Override
    public void addMerchantManager(Merchant merchant, long loginId, Set<ManageLevel> set) {
        Login login = loginService.findOne(loginId);
        // 如果这个用户是平台管理员则血崩
        if (login.getLevelSet().stream().anyMatch(Login.rootLevel::contains)) {
            throw new IllegalArgumentException("平台管理员无法被设置为商户管理员");
        }
        if (login.getLevelSet().contains(ManageLevel.merchantRoot))
            throw new IllegalArgumentException("它已经是一个商户主了。");

        login.setMerchant(merchant);
        login.setLevelSet(set);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void freezeOrEnable(long id, boolean enable) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne(id);
        if (merchant == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MERCHANT_NOT_EXIST));
        }
        merchant.setEnabled(enable);
        merchantRepository.save(merchant);
    }

    @Override
    public Merchant findOne(long merchantId) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne(merchantId);
        if (merchant == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_EXIST));
        }
        return merchant;
    }

    @Override
    public Merchant findMerchant(long merchantId) throws ApiResultException {
        Merchant merchant = findOne(merchantId);
        if (!merchant.isEnabled())
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MERCHANT_NOT_ENABLE));
        return merchant;
    }
}
