package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.RepresentRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.RepresentService;
import com.ming.shopping.beauty.service.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author helloztt
 */
@Service
public class RepresentServiceImpl implements RepresentService {
    @Autowired
    private RepresentRepository representRepository;
    @Autowired
    private LoginService loginService;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private StoreService storeService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Represent addRepresent(long loginId, long storeId) {
        Login login = loginService.findOne(loginId);
        if (login.getRepresent() != null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_REPRESENT_EXIST));
        }
        Store store = storeService.findStore(storeId);
        Represent represent = new Represent();
        represent.setId(loginId);
        represent.setLogin(login);
        represent.setStore(store);
        represent.setCreateTime(LocalDateTime.now());
        login.setRepresent(represent);
        login.setStore(store);
        login.addLevel(ManageLevel.represent);
        return representRepository.save(represent);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void freezeOrEnable(long id, boolean enable) {
        Represent represent = findOne(id);
        represent.setEnable(enable);
    }

    @Override
    public Represent findOne(long id) {
        Represent represent = representRepository.findOne(id);
        if (represent == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.REPRESENT_NOT_EXIST));
        }
        return represent;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void removerRepresent(long storeId, long id) {
        Represent represent = representRepository.findOne(id);
        if (represent != null) {
            Login login = loginRepository.getOne(id);
            if(login != null){
                login.setRepresent(null);
                Store store = storeService.findStore(storeId);
                List<Represent> represents = store.getRepresents();
                represents.remove(represent);
                store.setRepresents(represents);
            }else
                throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_EXIST));
        }else
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.REPRESENT_NOT_EXIST));
    }

}
