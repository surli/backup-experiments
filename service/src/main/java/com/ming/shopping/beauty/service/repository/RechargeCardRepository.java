package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by helloztt on 2018/1/4.
 */
public interface RechargeCardRepository extends JpaRepository<RechargeCard, Long>, JpaSpecificationExecutor<RechargeCard> {

    /**
     * 获取卡密重复的充值卡
     *
     * @return
     */
    @Query("SELECT rc FROM RechargeCard rc WHERE rc.code IN (SELECT rc.code FROM RechargeCard rc GROUP BY rc.code HAVING count(rc.code)>1)")
    List<RechargeCard> repetitiveCode();

    List<RechargeCard> findByBatch_EmailAddress(String email);
}
