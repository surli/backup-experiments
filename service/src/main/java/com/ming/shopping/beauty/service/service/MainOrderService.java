package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.model.request.ItemNum;
import com.ming.shopping.beauty.service.model.request.OrderSearcherBody;
import me.jiangcai.crud.row.FieldDefinition;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author lxf
 */
public interface MainOrderService {


    /**
     * @param id 订单id
     * @return 某个订单
     */
    MainOrder findById(long id);

    /**
     * 给用户增加一个空的订单
     *
     * @param user
     * @return
     */
    @Transactional
    MainOrder newEmptyOrder(User user);

    /**
     * 生成订单
     *
     * @param orderId 订单编号
     * @param login   负责下单的店方代表
     * @param store   特定门店
     * @param items   下单门店项目及数量
     * @return
     */
    MainOrder supplementOrder(long orderId, Login login, Store store, ItemNum[] items);

    /**
     * 生成订单
     *
     * @param orderId 扫二维码生成的订单
     * @param login   负责下单的店方代表
     * @param store   特定门店
     * @param amounts 下单的门店数量
     * @return
     */
    @Transactional
    MainOrder supplementOrder(long orderId, Login login, Store store, Map<StoreItem, Integer> amounts);

    /**
     * 生成订单
     *
     * @param orderId   扫二维码生成的订单
     * @param login     负责下单的店方代表
     * @param store     特定门店
     * @param storeItem 门店项目
     * @param amount    下单数量
     * @return
     */
    @Transactional
    MainOrder supplementOrder(long orderId, Login login, Store store, StoreItem storeItem, int amount);

    /**
     * 支付订单
     *
     * @param id 被支付的订单.
     * @return 是否成功
     */
    @Transactional
    void payOrder(long id);

    /**
     * 根据条件查询订单
     *
     * @param orderSearcher
     * @return
     */
    Page findAll(OrderSearcherBody orderSearcher);

    /**
     * 查询订单字段及定义
     *
     * @return
     */
    List<FieldDefinition<MainOrder>> orderListField();

}
