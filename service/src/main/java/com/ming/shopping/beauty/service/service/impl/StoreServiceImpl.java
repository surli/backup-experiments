package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.StoreRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.jpa.entity.support.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * @author helloztt
 */
@Service
public class StoreServiceImpl implements StoreService {
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private LoginService loginService;
    @Autowired
    private MerchantService merchantService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Store addStore(long loginId, long merchantId, String name, String telephone, String contact, Address address) {
        Login login = loginService.findOne(loginId);
        if (login.getStore() != null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_STORE_EXIST));
        }
        Merchant merchant = merchantService.findMerchant(merchantId);
        Store store = new Store();
        store.setId(loginId);
        store.setLogin(login);
        store.setMerchant(merchant);
        store.setName(name);
        store.setTelephone(telephone);
        store.setAddress(address);
        store.setContact(contact);
        store.setCreateTime(LocalDateTime.now());
        storeRepository.save(store);
        login.setStore(store);
        login.addLevel(ManageLevel.storeRoot);
        merchant.getStores().add(store);
        return store;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void freezeOrEnable(long id, boolean enable) {
        Store store = findOne(id);
        if (store == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.STORE_NOT_EXIST));
        }
        store.setEnabled(enable);
    }

    @Override
    public Store findOne(long id) throws ApiResultException {
        Store store = storeRepository.findOne(id);
        if (store == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.STORE_NOT_EXIST));
        }
        if (!store.isEnabled()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.STORE_NOT_ENABLE));
        }
//        if (!store.isManageable() && !store.isEnabled()) {
//            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MANAGE_NOT_ENABLE));
//        }
        return store;
    }

    @Override
    public Store findStore(long storeId) {
        return findOne(storeId);
    }

    @Override
    public Store findByLogin(Login login) throws ApiResultException {
        return storeRepository.findOne(((root, query, cb) -> cb.and(cb.equal(root.get(Store_.login), login))));
    }
}
