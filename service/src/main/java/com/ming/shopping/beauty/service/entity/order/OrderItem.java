package com.ming.shopping.beauty.service.entity.order;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 其实就是为了保存下单时 项目数据和下单数量 的
 *
 * @author helloztt
 */
@Entity
@Setter
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne
    private MainOrder mainOrder;

    @ManyToOne
    private Item item;

    /**
     * 项目名称
     */
    @Column(length = 40)
    private String name;
    /**
     * 原价
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal price;
    /**
     * 销售价
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal salesPrice;
    /**
     * 结算价
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal costPrice;
    /**
     * 数量
     */
    @Column(precision = Constant.FLOAT_COLUMN_PRECISION)
    private int num;
}
