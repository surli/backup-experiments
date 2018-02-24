package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.order.MainOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface MainOrderRepository extends JpaRepository<MainOrder, Long>, JpaSpecificationExecutor<MainOrder> {

    /**
     * 查询用户的空订单
     *
     * @param userId 用户编号
     * @return
     */
    @Query("select o from MainOrder o where o.payer.id = ?1 " +
            "and o.orderStatus = com.ming.shopping.beauty.service.entity.support.OrderStatus.EMPTY")
    MainOrder findEmptyOrderByPayer(long userId);

    /**
     * 统计用户历史消费总额
     *
     * @param userId 用户编号
     * @return
     */
    @Query("select sum(o.finalAmount) from MainOrder o where o.payer.id = ?1 " +
            "and o.orderStatus = com.ming.shopping.beauty.service.entity.support.OrderStatus.success")
    BigDecimal sumFinalAmountLByPayer(long userId);
}
