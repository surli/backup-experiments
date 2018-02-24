package com.ming.shopping.beauty.service.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author helloztt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemNum {
    /**
     * 实际上是item id；保持前端协议所以这里并不更正名称
     *
     * @see com.ming.shopping.beauty.service.entity.item.Item#id
     */
    private long storeItemId;
    private int num;
}
