package com.ming.shopping.beauty.service.service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author CJ
 */
public interface StagingService {
    /**
     * 构造staging环境
     *
     * @throws IOException
     */
    void initStagingEnv() throws IOException, ClassNotFoundException;

    @PostConstruct
    void init() throws IOException, ClassNotFoundException;

    /**
     * 一个商户，一个门店，一个门店代表
     * 商户一共建立了5个项目；
     * 已审核，enabled, 到门店，门店enabled
     * 已审核，enabled, 到门店，门店未enabled
     * 已审核，没enabled, 到门店，门店enabled
     * 非已审核；，enabled, 到门店，门店enabled
     * 已审核，enabled, 未到门店
     *
     * @return 供staging使用的测试数据;一个商户，一个门店，一个门店代表，以及一堆项目
     * @throws IOException
     */
    Object[] generateStagingData() throws IOException;

    /**
     * 注册登录流程需要的数据
     * 20张充值卡
     * @return
     * @throws IOException
     */
    Object[] registerStagingData() throws IOException, ClassNotFoundException;
}
