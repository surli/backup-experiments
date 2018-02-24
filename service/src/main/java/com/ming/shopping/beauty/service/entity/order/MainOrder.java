package com.ming.shopping.beauty.service.entity.order;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * 订单
 * @author helloztt
 */
@Entity
@Setter
@Getter
public class MainOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    /**
     * 门店
     */
    @ManyToOne
    private Store store;
    /**
     * 付款用户
     */
    @ManyToOne
    private User payer;
    /**
     * 下单门店代表
     */
    @ManyToOne
    private Login login;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "mainOrder")
    private List<OrderItem> orderItemList;
    /**
     * 实际应付金额
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal finalAmount;

    /**
     * 成本金额
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal settlementAmount;
    /**
     * 订单状态
     */
    private OrderStatus orderStatus;
    /**
     * 生成时间
     */
    @Column(columnDefinition = Constant.DATE_NULLABLE_COLUMN_DEFINITION)
    private LocalDateTime createTime;
    /**
     * 支付时间
     */
    @Column(columnDefinition = Constant.DATE_NULLABLE_COLUMN_DEFINITION)
    private LocalDateTime payTime;

    /**
     * 是否结算
     */
    private boolean settled;
    /**
     * 结算单
     */
    @ManyToOne
    private SettlementSheet settlementSheet;
    /**
     * 订单总金额
     */
    public BigDecimal getTotalAmount(){
        return withAmount(OrderItem::getSalesPrice);
    }
    /**
     * 结合数量结算金额
     *
     * @param function 每个商品所牵涉金额
     * @return 总牵涉金额
     */
    protected BigDecimal withAmount(Function<OrderItem, BigDecimal> function) {
        BigDecimal current = BigDecimal.ZERO;
        for (OrderItem orderItem:orderItemList) {
            BigDecimal one = function.apply(orderItem);
            current = current.add(one.multiply(BigDecimal.valueOf(orderItem.getNum())));
        }
        return current.setScale(Constant.FLOAT_COLUMN_SCALE,Constant.ROUNDING_MODE);
    }

}
