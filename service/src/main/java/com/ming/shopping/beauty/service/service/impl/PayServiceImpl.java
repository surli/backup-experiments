package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.order.RechargeOrder;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.repository.RechargeOrderRepository;
import com.ming.shopping.beauty.service.service.PayService;
import me.jiangcai.payment.event.OrderPayCancellation;
import me.jiangcai.payment.event.OrderPaySuccess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author helloztt
 */
@Service
public class PayServiceImpl implements PayService {
    private static final Log log = LogFactory.getLog(PayServiceImpl.class);

    @Autowired
    private RechargeOrderRepository rechargeOrderRepository;

    @Override
    public void paySuccess(OrderPaySuccess event) {
        log.info("处理付款成功事件");
        if (event.getPayableOrder() instanceof RechargeOrder) {
            RechargeOrder rechargeOrder = (RechargeOrder) event.getPayableOrder();
            if (rechargeOrder.isPay()) {
                log.warn("订单已支付，却发起了重复事件。" + rechargeOrder.getSerialId());
                return;
            }
//                throw new IllegalStateException("订单已支付");
            rechargeOrder.setPayTime(LocalDateTime.now());
            rechargeOrder.setOrderStatus(OrderStatus.success);
        }
    }

    @Override
    public void payCancel(OrderPayCancellation event) {
        log.info(event.getPayableOrder() + "放弃了支付");
        rechargeOrderRepository.delete(event.getPayOrder().getId());
    }
}
