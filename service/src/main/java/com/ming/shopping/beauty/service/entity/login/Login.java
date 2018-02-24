package com.ming.shopping.beauty.service.entity.login;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;
import me.jiangcai.wx.standard.entity.StandardWeixinUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ming.shopping.beauty.service.utils.Constant.DATE_COLUMN_DEFINITION;

/**
 * 可登录的角色
 * Created by helloztt on 2017/12/26.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Setter
@Getter
public class Login implements UserDetails, CrudFriendly<Long> {
    /**
     * 商户超管
     */
    public static final String ROLE_MERCHANT_ROOT = "MERCHANT_ROOT";
    /**
     * 商户可以管理操作员的权限
     */
    public static final String ROLE_MERCHANT_MANAGE = "MERCHANT_MANAGE";
    /**
     * 商户读取信息权限
     */
    public static final String ROLE_MERCHANT_READ = "MERCHANT_READ";
    /**
     * 商户可以管理项目的权限
     */
    public static final String ROLE_MERCHANT_ITEM = "MERCHANT_ITEM";
    /**
     * 商户可以管理门店的权限
     */
    public static final String ROLE_MERCHANT_STORE = "MERCHANT_STORE";
    /**
     * 商户可以结算的权限
     */
    public static final String ROLE_MERCHANT_SETTLEMENT = "MERCHANT_SETTLEMENT";
    /**
     * 门店超管及操作员
     */
    public static final String ROLE_STORE_ROOT = "STORE_ROOT";
    public static final String ROLE_STORE_REPRESENT = "STORE_REPRESENT";
    /**
     * 审核项目
     */
    public static final String ROLE_PLATFORM_AUDIT_ITEM = "AUDIT_ITEM";
    /**
     * 管理商户的权限
     */
    public static final String ROLE_PLATFORM_MERCHANT = "PLATFORM_MERCHANT";
    /**
     * 结算
     */
    public static final String ROLE_PLATFORM_SETTLEMENT = "PLATFORM_SETTLEMENT";
    /**
     * 平台管理员都具备的权限
     */
    public static final String ROLE_PLATFORM_READ = "PLATFORM_READ";

    /**
     * 平台管理员有哪些角色
     */
    public static final List<ManageLevel> rootLevel = Arrays.asList(
            ManageLevel.root
            , ManageLevel.rootGeneral
            , ManageLevel.rootSettlementManager
            , ManageLevel.rootItemManager
            , ManageLevel.rootMerchantManager
    );
    /**
     * 商户操作员有哪些角色
     */
    public static final List<ManageLevel> merchantLevel = Arrays.asList(
            ManageLevel.merchantRoot
            , ManageLevel.merchantSettlementManager
            , ManageLevel.merchantItemManager
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 这个身份所关联的用户，通常应该是唯一的
     */
    @OneToOne
    private StandardWeixinUser wechatUser;

    @Column(length = 30)
    private String loginName;
    /**
     * 可推荐别人
     */
    private boolean guidable;
    /**
     * 关联商户
     */
    @OneToOne
    @JsonIgnore
    private Merchant merchant;
    /**
     * 关联门店
     */
    @OneToOne
    @JsonIgnore
    private Store store;
    /**
     * 必定有 user ，但可能没激活
     */
    @OneToOne
    @JsonIgnore
    private User user;
    /**
     * 门店代表
     */
    @OneToOne
    @JsonIgnore
    private Represent represent;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<ManageLevel> levelSet;

    @Column(columnDefinition = DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;
    /**
     * 冻结或删除都应设置为 false
     */
    private boolean enabled = true;

    @Column(name = "`DELETE`")
    private boolean delete;
    /**
     * 用户历史消费总额
     */
    @Transient
    private BigDecimal consumption;

    /**
     * @param from login from
     * @return 获取是否为管理员的表达式
     */
    public static Predicate getManageableExpr(From<?, Login> from) {
        return from.get(Login_.levelSet).in(
                rootLevel
        );
    }

    public static Expression<BigDecimal> getCurrentBalanceExpr(From<?, Login> from, CriteriaBuilder cb) {
        Join<Login, User> userJoin = from.join(Login_.user, JoinType.LEFT);
        return cb.<Boolean, BigDecimal>selectCase(cb.isNull(userJoin))
                .when(true, BigDecimal.ZERO)
                .otherwise(User.getCurrentBalanceExpr(userJoin, cb));
    }

    public static Collection<? extends GrantedAuthority> getGrantedAuthorities(Set<ManageLevel> levelSet) {
        if (CollectionUtils.isEmpty(levelSet)) {
            return Collections.emptySet();
        }
        Stream<String> fixed = levelSet.stream()
                .flatMap(level1 -> Stream.of(level1.roles()));
        return fixed
                .map(ManageLevel::roleNameToRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getGrantedAuthorities(levelSet);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return loginName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !delete;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public void addLevel(ManageLevel... manageLevels) {
        if (levelSet == null) {
            levelSet = new HashSet<>();
        }
        levelSet.addAll(Arrays.asList(manageLevels));
    }


    public String getHumanReadName() {
        if (wechatUser == null)
            return loginName;
        return wechatUser.getNickname();
    }

    public static Predicate nameMatch(From<?, Login> from, CriteriaBuilder cb, String input) {
        return cb.or(
                cb.like(from.join(Login_.wechatUser, JoinType.LEFT).get("nickname"), "%" + input + "%")
                , cb.like(from.get(Login_.loginName), "%" + input + "%")
        );
    }
}
