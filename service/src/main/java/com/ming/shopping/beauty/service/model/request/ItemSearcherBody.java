package com.ming.shopping.beauty.service.model.request;

import lombok.Data;

/**
 * @author helloztt
 */
@Data
public class ItemSearcherBody {

    private Long merchantId;

    private Long storeId;

    private Boolean enabled;

    private Boolean recommended;

    // TODO: 2018/1/13 不确定会不会用到，待补充
}
