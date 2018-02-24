package com.ming.shopping.beauty.service.entity.item;

import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 平台钱包充值卡
 *
 * @author helloztt
 */
@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})})
public class RechargeCard implements Cloneable, CrudFriendly<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 卡密，需要唯一
     */
    @Column(length = User.CARD_NO_LEN)
    private String code;
    /**
     * 是否已被兑换
     */
    private boolean used;

    @ManyToOne
    private User user;
    /**
     * 金额
     */
    @Column(precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal amount;
    /**
     * 兑换时间
     */
    @Column(columnDefinition = Constant.DATE_NULLABLE_COLUMN_DEFINITION)
    private LocalDateTime usedTime;
    @ManyToOne
    private RechargeCardBatch batch;

    @Override
    public Object clone() {
        RechargeCard card = null;
        try {
            card = (RechargeCard) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return card;
    }
}
