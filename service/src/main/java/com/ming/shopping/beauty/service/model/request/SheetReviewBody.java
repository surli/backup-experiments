package com.ming.shopping.beauty.service.model.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lxf
 */
@Data
public class SheetReviewBody {

    /**
     * 修改的状态
     */
    private String status;
    /**
     * 备注
     */
    private String comment;
    /**
     * 支付的金额
     */
    private BigDecimal amount;
}
