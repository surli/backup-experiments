package com.ming.shopping.beauty.service.entity.support;

import com.ming.shopping.beauty.service.entity.login.Login;
import me.jiangcai.lib.sys.SystemStringConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 管理员级别
 *
 * @author CJ
 */
public enum ManageLevel {
    root("超级管理员", "ROOT"),
    /**
     * 啥都能干的那种
     */
    rootGeneral("经理", SystemStringConfig.MANAGER_ROLE, Login.ROLE_PLATFORM_SETTLEMENT, Login.ROLE_PLATFORM_AUDIT_ITEM
            , Login.ROLE_PLATFORM_MERCHANT, Login.ROLE_PLATFORM_READ),
    /**
     * 平台管理员，结算相关
     */
    rootSettlementManager("财务", Login.ROLE_PLATFORM_SETTLEMENT, Login.ROLE_PLATFORM_READ),
    /**
     * 管理员， 审核项目
     * 查看项目，审核项目，上下架项目
     */
    rootItemManager("审核专员", Login.ROLE_PLATFORM_AUDIT_ITEM, Login.ROLE_PLATFORM_READ),
    rootMerchantManager("商户专员", Login.ROLE_PLATFORM_MERCHANT, Login.ROLE_PLATFORM_READ),


    /**
     * 商户管理员
     */
    merchantRoot("商户管理员", Login.ROLE_MERCHANT_ROOT, Login.ROLE_MERCHANT_MANAGE, Login.ROLE_MERCHANT_ITEM
            , Login.ROLE_MERCHANT_STORE, Login.ROLE_MERCHANT_READ),
    /**
     * 商户操作员， 管理项目及门店项目
     */
    merchantItemManager("商户操作员", Login.ROLE_MERCHANT_ITEM, Login.ROLE_MERCHANT_STORE, Login.ROLE_MERCHANT_READ),
    /**
     * 商户操作员，结算相关
     */
    merchantSettlementManager("商户财务", Login.ROLE_MERCHANT_SETTLEMENT, Login.ROLE_MERCHANT_READ),


    /**
     * 门店管理员
     */
    storeRoot("门店管理员", Login.ROLE_STORE_ROOT),
//    /**
//     * 门店操作员
//     */
//    storeMerchant("门店操作员", Login.ROLE_STORE_OPERATOR),
    /**
     * 门店代表
     */
    represent("门店代表", Login.ROLE_STORE_REPRESENT),
    /**
     * 用户
     */
    user("用户", "USER"),
    //将来添加角色,原数据不能删除,只能在这之后向下加.
    ;
    private final String[] roles;
    private final String title;

    ManageLevel(String title, String... roles) {
        this.title = title;
        this.roles = roles;
    }

    public static String roleNameToRole(String role) {
        String role2 = role.toUpperCase(Locale.CHINA);
        if (role2.startsWith("ROLE_"))
            return role2;
        return "ROLE_" + role2;
    }

    /**
     * @return role names
     */
    public String[] roles() {
        return roles;
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return Stream.of(roles)
                .map(ManageLevel::roleNameToRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public String title() {
        return title;
    }
}
