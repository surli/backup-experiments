package com.ming.shopping.beauty.service.model.request;

import lombok.Data;

/**
 * @author lxf
 */
@Data
public class NewMerchantBody {
    Long id;
    /**
     * 商户名
     */
    private String name;
    /**
     * 电话
     */
    private String telephone;
    /**
     * 联系人
     */
    private String contact;
    /**
     * 绑定的Loginid
     */
    private long loginId;
}
