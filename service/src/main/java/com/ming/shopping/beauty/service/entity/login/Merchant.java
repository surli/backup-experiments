package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;
import me.jiangcai.jpa.entity.support.Address;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.ming.shopping.beauty.service.utils.Constant.DATE_COLUMN_DEFINITION;

/**
 * 商户
 *
 * @author lxf
 */
@Entity
@Getter
@Setter
public class Merchant implements CrudFriendly<Long> {
    @Id
    private Long id;
    /**
     * share primary key
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.PERSIST}, optional = false)
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
    private Login login;
    /**
     * 商户名称
     */
    @Column(length = 50)
    private String name;

    /**
     * 联系电话
     */
    @Column(length = 20)
    private String telephone;

    /**
     * 联系人
     */
    @Column(length = 50)
    private String contact;

    /**
     * 地址
     * TODO 要用新的一套
     */
    private Address address;
    /**
     * 冻结或删除都应设置为 false
     */
    private boolean enabled = true;

    /**
     * 商户拥有的门店.
     */
    @OneToMany
    @OrderBy("createTime desc")
    private List<Store> stores;

    @Column(columnDefinition = DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Merchant)) return false;
        Merchant merchant = (Merchant) o;
        return Objects.equals(id, merchant.id);
    }

    public void fromRequest(Merchant merchant) {
        setTelephone(merchant.telephone);
        setContact(merchant.contact);
        setName(merchant.name);
        setAddress(merchant.address);
    }
}
