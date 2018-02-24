package com.ming.shopping.beauty.service.model.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lxf
 */
@Data
public class NewItemBody {
    //id
    private Long id;
    //所属商户
    private long merchantId;
    //
    //项目名
    private String name;
    private String imagePath;
    //itemType
    private String itemType;

    //原价
    private BigDecimal price;
    //会员价/销售价
    private BigDecimal salesPrice;
    //结算价
    private BigDecimal costPrice;

    //简述
    private String description;
    //富文本描述
    private String richDescription;
}
