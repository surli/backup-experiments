package com.ming.shopping.beauty.manage.modal;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CJ
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MerchantCreation extends Merchant {
    private Long loginId;
}
