package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


/**
 * @author
 */
public interface MerchantService {

    /**
     * 添加商户，并且将loginId设置为商户主
     *
     * @param loginId  一个可登录的角色
     * @param merchant 商户基本信息，包含名称、电话、联系人、地址
     * @return
     * @throws ApiResultException
     */
    @Transactional(rollbackFor = RuntimeException.class)
    Merchant addMerchant(long loginId, Merchant merchant) throws ApiResultException;

    /**
     * 添加商户操作员
     *
     * @param merchant 商户
     * @param loginId  操作员id
     * @param set      权限集合
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void addMerchantManager(Merchant merchant, long loginId, Set<ManageLevel> set);

    /**
     * 冻结或启用商户
     *
     * @param id
     * @param enable 是否启用
     * @throws ApiResultException
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void freezeOrEnable(long id, boolean enable) throws ApiResultException;

    /**
     * 查找商户或商户管理员，同时检查商户或商户管理员是否可用，如果不可用就抛出异常
     *
     * @param merchantId 商户或商户管理员
     * @return
     * @throws ApiResultException
     */
    Merchant findOne(long merchantId) throws ApiResultException;

    /**
     * 查找商户，同时检查商户是否可用，如果不可用就抛出异常
     *
     * @param merchantId
     * @return
     * @throws ApiResultException
     */
    Merchant findMerchant(long merchantId) throws ApiResultException;

}
