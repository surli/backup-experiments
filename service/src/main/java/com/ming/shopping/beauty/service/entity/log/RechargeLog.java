package com.ming.shopping.beauty.service.entity.log;

import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值/提现记录，来源：手动充值,手动扣款
 * @author helloztt
 */
@Entity
@Getter
@Setter
public class RechargeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(scale = Constant.FLOAT_COLUMN_SCALE,precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal amount;

    @ManyToOne
    private User user;
    /**
     * 充值卡编号
     */
    private long rechargeCardId;
    /**
     * 第三方支付订单编号
     */
    private String payOrderId;

    /**
     * 充值类型
     */
    private RechargeType rechargeType;

    @Column(columnDefinition = Constant.DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;
}
