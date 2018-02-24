package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * @author lxf
 */
public interface OrderItemService {

    /**
     * 根据订单查询订单中的项目
     * @return 该订单所有的项目
     */
    List<OrderItem> findByOrderId(long id);

    /**
     * @param item 所属项目
     * @param num 数量
     * @return 该OrderItem
     */
    OrderItem newOrderItem(Item item,int num);

    /**
     * @param itemMap 项目和数量的集合
     * @return
     */
    List<OrderItem> newOrderItems(Map<Item,Integer> itemMap);
}
