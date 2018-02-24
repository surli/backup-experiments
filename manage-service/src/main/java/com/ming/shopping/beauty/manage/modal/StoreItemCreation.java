package com.ming.shopping.beauty.manage.modal;

import com.ming.shopping.beauty.service.entity.item.StoreItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CJ
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StoreItemCreation extends StoreItem {
    private Long storeId;
    private Long itemId;
}
