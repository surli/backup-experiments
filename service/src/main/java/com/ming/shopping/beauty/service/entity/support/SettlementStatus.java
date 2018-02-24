package com.ming.shopping.beauty.service.entity.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author  lxf
 */
@Getter
@AllArgsConstructor
public enum SettlementStatus {

    //待提交、待审核、已打回、已审核、已打款、已收款，已撤销
    UNSUBMIT("待提交"),
    TO_AUDIT("待审核"),
    APPROVAL("已审核"),
    REJECT("已打回"),
    ALREADY_PAID("已打款"),
    COMPLETE("已收款"),
    REVOKE("已撤销");

    private String message;

}
