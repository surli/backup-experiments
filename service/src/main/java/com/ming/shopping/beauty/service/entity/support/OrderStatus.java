package com.ming.shopping.beauty.service.entity.support;

/**
 * @author helloztt
 */
public enum  OrderStatus {
    //TODO
    EMPTY("E"),
    /**
     * 等待支付
     */
    forPay("待付款"),
    /**
     * 已完成
     */
    success("已完成"),
    /**
     * 已关闭
     */
    close("已关闭");

    private final String message;

    OrderStatus(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
