package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author CJ
 */
public interface RechargeCardBatchRepository extends JpaRepository<RechargeCardBatch, Long>
        , JpaSpecificationExecutor<RechargeCardBatch> {
}
