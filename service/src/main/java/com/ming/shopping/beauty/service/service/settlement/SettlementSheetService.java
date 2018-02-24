package com.ming.shopping.beauty.service.service.settlement;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author lxf
 */
public interface SettlementSheetService {

    /**
     * 查找一个结算单
     */
    SettlementSheet findSheet(long id);

    /**
     * 商户获取内结算单(创建一个未提交的结算单)
     * 随时可以发起的, 但是不统计规定时间内的订单
     * @param merchant 统计结算单的商户
     * @return 统计好的结算单
     */
    @Transactional
    SettlementSheet addSheet(Merchant merchant);

    /**
     * 商户递交结算单交给平台财务审核
     * @param settlementSheet 提交的结算单
     * @param comment 备注
     */
    @Transactional
    void submitSheet(SettlementSheet settlementSheet,String comment);

    /**
     * 审核没通过, 被打回
     * @param settlementSheet 结算单
     * @param comment 备注
     */
    @Transactional
    void rejectSheet(SettlementSheet settlementSheet,String comment);

    /**
     * 商户撤销 未提交或被打回的结算单
     * @param settlementSheet 结算单
     */
    @Transactional
    void revokeSheet(SettlementSheet settlementSheet);


    /**
     * 平台管理员通过审核的结算单
     * @param settlementSheet 结算单
     * @param comment 备注
     */
    @Transactional
    void approvalSheet(SettlementSheet settlementSheet, String comment);

    /**
     * 平台管理员已经支付过结算单
     * @param settlementSheet 结算单
     * @param amount 实际转账金额
     */
    @Transactional
    void alreadyPaid(SettlementSheet settlementSheet, BigDecimal amount);

    /**
     * 商户收到结算款,确认结算完成
     * @param settlementSheet 结算单
     */
    @Transactional
    void completeSheet(SettlementSheet settlementSheet);

    /**
     * 修改enabled, 使结算单不在显示出来
     * @param settlementSheet
     * @param delete
     */
    @Transactional
    void putEnabled(SettlementSheet settlementSheet,boolean delete);
}
