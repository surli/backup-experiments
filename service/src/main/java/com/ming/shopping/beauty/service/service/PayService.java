package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.order.MainOrder;
import me.jiangcai.payment.entity.PayOrder;
import me.jiangcai.payment.event.OrderPayCancellation;
import me.jiangcai.payment.event.OrderPaySuccess;
import me.jiangcai.payment.service.PayableSystemService;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author helloztt
 */
public interface PayService {

    @EventListener(OrderPaySuccess.class)
    @Transactional
    void paySuccess(OrderPaySuccess event);

    @EventListener(OrderPayCancellation.class)
    @Transactional
    void payCancel(OrderPayCancellation event);
}
