package com.ming.shopping.beauty.manage.modal;

import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CJ
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RechargeCardBatchCreation extends RechargeCardBatch {
    private int number;
    private long guideId;
}
