package com.ming.shopping.beauty.service.model.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * @author helloztt
 */
@Data
public class DepositBody {
    /**
     * 充值金额
     */
    private BigDecimal depositSum;
    /**
     * 卡密
     */
    @Size(min = 20,max = 20,message = "卡密")
    private String cdKey;
    /**
     * 支付成功后跳转地址
     */
    private String redirectUrl;

}
