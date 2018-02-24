package com.ming.shopping.beauty.manage.modal;

import com.ming.shopping.beauty.service.entity.login.Store;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CJ
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StoreCreation extends Store {
    private Long loginId;
    private Long merchantId;
}
