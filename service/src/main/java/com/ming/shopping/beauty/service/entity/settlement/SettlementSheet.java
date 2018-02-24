package com.ming.shopping.beauty.service.entity.settlement;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.support.SettlementStatus;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author lxf
 */
@Entity
@Getter
@Setter
public class SettlementSheet implements CrudFriendly<Long> {
    /**
     * 结算单编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属商户
     */
    @ManyToOne
    private Merchant merchant;

    /**
     * 关联的订单
     */
    @OneToMany
    private Set<MainOrder> mainOrderSet;

    /**
     * 变动金额
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal variableAmount;

    /**
     * 实际转账金额
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal actualAmount;


    private SettlementStatus settlementStatus;
    /**
     * 备注
     */
    @Column(length = 100)
    private String comment;

    /**
     * 生成时间
     */
    @Column(columnDefinition = Constant.DATE_NULLABLE_COLUMN_DEFINITION)
    private LocalDateTime createTime;

    /**
     * 打款时间
     */
    @Column(columnDefinition = Constant.DATE_NULLABLE_COLUMN_DEFINITION)
    private LocalDateTime transferTime;

    /**
     * 在为true的情况下,不在列表里面显示.
     */
    private boolean detect;

}
