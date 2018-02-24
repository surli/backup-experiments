package com.ming.shopping.beauty.manage.modal;

import com.ming.shopping.beauty.service.entity.item.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CJ
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ItemCreation extends Item {

    private Long merchantId;
    private String imagePath;

}
