package com.ming.shopping.beauty.service.model.request;

import lombok.Data;

/**
 * @author lxf
 */
@Data
public class NewStoreBody {

    /**
     * 商户id
     */
    private long merchantId;
    /**
     * 变成门店的用户
     */
    private long loginId;

    /**
     * 门店名字
     */
    private String name;
    /**
     * 门店联系人
     */
    private String contact;

    /**
     * 门店电话
     */
    private String telephone;

}
