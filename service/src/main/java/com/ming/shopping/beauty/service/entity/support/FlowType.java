package com.ming.shopping.beauty.service.entity.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author helloztt
 */
@AllArgsConstructor
@Getter
public enum  FlowType {
    PLATFORM_CHANGE("后台调整"),
    RECHARGE_CARD("充值卡充值"),
    PAY("在线充值"),
    OUT("订单消费");
    private final String message;

    @Override
    public String toString() {
        return message;
    }
}
