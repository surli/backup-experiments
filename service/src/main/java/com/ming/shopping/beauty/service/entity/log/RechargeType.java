package com.ming.shopping.beauty.service.entity.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 充值类型  手动充值, 充值卡充值.
 * @author lxf
 */
@AllArgsConstructor
@Getter
public enum RechargeType {

    MANUAL("手动充值"),
    DEDUCTION("手动扣款");

    private String message;

    @Override
    public String toString() {
        return message;
    }
}
