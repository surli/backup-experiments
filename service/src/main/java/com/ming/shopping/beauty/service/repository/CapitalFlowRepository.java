package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.log.CapitalFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface CapitalFlowRepository extends JpaRepository<CapitalFlow,Long>,JpaSpecificationExecutor<CapitalFlow> {

//    @Query("select sum(c.changed) from CapitalFlow c where c.userId = ?1")
//    BigDecimal findBalanceByUserId(Long userId);
}
