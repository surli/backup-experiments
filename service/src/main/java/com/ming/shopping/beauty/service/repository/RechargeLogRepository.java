package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.log.RechargeLog;
import com.ming.shopping.beauty.service.entity.login.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author helloztt
 */
public interface RechargeLogRepository extends JpaRepository<RechargeLog,Long>,JpaSpecificationExecutor<RechargeLog> {
    List<RechargeLog> findByUser(User one);
}
