package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.wx.model.Gender;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.ming.shopping.beauty.service.utils.Constant.DATE_COLUMN_DEFINITION;

/**
 * 门店代表：其实就是门店的收营员，负责帮用户下单
 * @author lxf
 */
@Entity
@Setter
@Getter
public class Represent {
    @Id
    private Long id;
    /**
     * share primary key
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @PrimaryKeyJoinColumn(name = "id",referencedColumnName = "id")
    private Login login;
    /**
     * 所属门店
     */
    @ManyToOne
    private Store store;

    @Column(columnDefinition = DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;

    private boolean enable = true;
}
