package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.order.RechargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author helloztt
 */
public interface RechargeOrderRepository extends JpaRepository<RechargeOrder,Long>,JpaSpecificationExecutor<RechargeOrder> {
}
