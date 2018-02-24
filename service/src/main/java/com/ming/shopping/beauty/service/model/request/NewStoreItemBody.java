package com.ming.shopping.beauty.service.model.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lxf
 */
@Data
public class NewStoreItemBody {
    /**
     * 编辑时候的id
     */
    private Long id;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 项目的id
     */
    private Long ItemId;


    /**
     * 销售价/会员价
     */
    private BigDecimal salesPrice;
}
