package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by helloztt on 2018/1/7.
 */
public class MerchantServiceTest extends CoreServiceTest {

    private Merchant mockMerchant;

    @Before
    public void init() throws Exception {
        mockMerchant = mockMerchant();
    }

    @Test
    public void addMerchant() throws Exception {
        assertThat(mockMerchant).isNotNull();
//        assertThat(mockMerchant.isManageable()).isTrue();
        //看看关联有没有问题
        assertThat(mockMerchant.getLogin()).isNotNull();
        assertThat(mockMerchant.getLogin().getMerchant()).isNotNull();
    }

//    @Test
//    public void addMerchantManage() throws Exception {
//        //单个级别
//        Merchant mockManage = mockMerchantManager(mockMerchant, ManageLevel.merchantItemManager);
//        assertThat(mockManage).isNotNull();
//        assertThat(mockManage.isManageable()).isFalse();
//        assertThat(mockManage.getMerchant()).isEqualTo(mockMerchant);
//        assertThat(mockManage.getLogin().getLevelSet()).contains(ManageLevel.merchantItemManager);
//
//        //多个级别
//        Merchant mockMultiLevelManage = mockMerchantManager(mockMerchant,ManageLevel.merchantItemManager,ManageLevel.merchantSettlementManager);
//        assertThat(mockMultiLevelManage).isNotNull();
//        assertThat(mockMultiLevelManage.isManageable()).isFalse();
//        assertThat(mockMultiLevelManage.getMerchant()).isEqualTo(mockMerchant);
//        assertThat(mockMultiLevelManage.getLogin().getLevelSet()).contains(ManageLevel.merchantItemManager,ManageLevel.merchantSettlementManager);
//    }

    @Test
    public void freezeOrEnable() throws Exception {
        assertThat(mockMerchant.isEnabled()).isTrue();
        merchantService.freezeOrEnable(mockMerchant.getId(), !mockMerchant.isEnabled());
        try {
            merchantService.findMerchant(mockMerchant.getId());
            throw new Exception();
        } catch (ApiResultException ex) {
            assertThat(ex.getApiResult().getCode()).isEqualTo(ResultCodeEnum.MERCHANT_NOT_ENABLE.getCode());
        }
        merchantService.freezeOrEnable(mockMerchant.getId(), mockMerchant.isEnabled());
    }

}