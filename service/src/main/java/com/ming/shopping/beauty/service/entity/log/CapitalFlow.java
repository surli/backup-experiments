package com.ming.shopping.beauty.service.entity.log;

import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.support.FlowType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author helloztt
 */
@Entity
@Getter
@Setter
public class CapitalFlow {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;
//    @Column(name = "USER_ID")
//    private Long userId;
    @Column(name = "ORDER_ID")
    private Long orderId;
    @Column(name = "HAPPEN_TIME")
    private LocalDateTime happenTime;
    @Column(name = "TYPE")
    private FlowType flowType;
    /**
     * 变化额，正数表示增加，负数表示减少
     */
    @Column(name = "CHANGED")
    private BigDecimal changed;

}
