package com.ming.shopping.beauty.service.model.request;

import lombok.Data;

/**
 * @author helloztt
 */
@Data
public class NewOrderBody {
    private long orderId;
    private ItemNum[] items;
}
