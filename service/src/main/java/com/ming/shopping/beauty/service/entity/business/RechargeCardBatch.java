package com.ming.shopping.beauty.service.entity.business;

import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
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
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 卡批次
 *
 * @author CJ
 */
@Entity
@Getter
@Setter
public class RechargeCardBatch implements CrudFriendly<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(orphanRemoval = true, mappedBy = "batch")
    private Set<RechargeCard> cardSet;

    /**
     * 接受卡密的人
     */
    @Column(length = 100)
    private String emailAddress;

    /**
     * 生成时间
     */
    @Column(columnDefinition = Constant.DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;

    /**
     * 这批卡的拓展用户
     */
    @ManyToOne(optional = false)
    private Login guideUser;

    /**
     * 生成这批卡的管理员
     */
    @ManyToOne
    private Login manager;
}
