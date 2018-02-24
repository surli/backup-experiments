package com.ming.shopping.beauty.service.repository.settlementSheet;

import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author  lxf
 */
public interface SettlementSheetRepository extends JpaRepository<SettlementSheet,Long>,JpaSpecificationExecutor<SettlementSheet> {
}
