package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.RechargeOrder;
import me.jiangcai.payment.event.OrderPaySuccess;
import org.springframework.context.event.EventListener;

import java.math.BigDecimal;

/**
 * @author helloztt
 */
public interface RechargeOrderService {
    /**
     * 新增充值订单
     *
     * @param payer  下单用户
     * @param amount 下单金额
     * @return
     */
    RechargeOrder newOrder(User payer, BigDecimal amount);

    /**
     * 充值成功
     * 1、如果会员未激活，就激活会员，添加 USER 权限，并且给他一个生成一个会员卡号；
     * 2、给用户账户上加钱
     *
     * @param event
     */
    @EventListener(OrderPaySuccess.class)
    void orderPaySuccess(OrderPaySuccess event);
}
