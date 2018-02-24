package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.RechargeOrder;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.repository.RechargeOrderRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.RechargeOrderService;
import me.jiangcai.payment.event.OrderPaySuccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author helloztt
 */
@Service
public class RechargeOrderServiceImpl implements RechargeOrderService {
    @Autowired
    private RechargeOrderRepository rechargeOrderRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public RechargeOrder newOrder(User payer, BigDecimal amount) {
        RechargeOrder rechargeOrder = new RechargeOrder();
        rechargeOrder.setPayer(payer);
        rechargeOrder.setAmount(amount);
        rechargeOrder.setCreateTime(LocalDateTime.now());
        return rechargeOrderRepository.save(rechargeOrder);
    }

    @Override
    @EventListener(OrderPaySuccess.class)
    @Transactional(rollbackFor = RuntimeException.class)
    public void orderPaySuccess(OrderPaySuccess event) {
        if (event.getPayableOrder() instanceof RechargeOrder) {
            RechargeOrder payOrder = (RechargeOrder) event.getPayableOrder();
            User payer = payOrder.getPayer();
            if (!payer.isActive()) {
                payer.setCardNo(User.makeCardNo());
                payer.getLogin().addLevel(ManageLevel.user);
            }
            userRepository.save(payOrder.getPayer());
        }
    }
}
