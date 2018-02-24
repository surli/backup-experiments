package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.model.request.OrderSearcherBody;
import com.ming.shopping.beauty.service.repository.OrderItemRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * @author lxf
 */
public class MainOrderServiceTest extends CoreServiceTest {
    @Autowired
    private MainOrderService mainOrderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Test
    public void go() throws Exception {
        //生成一个订单.
        Merchant merchant = mockMerchant();
        //首先有个项目
        Item item = mockItem(merchant);
        //生成门店项目
        Store store = mockStore(merchant);
        StoreItem storeItem = mockStoreItem(store,item);

        //生成一个订单
        Represent represent = mockRepresent(store);
        Login login = mockLogin();
        MainOrder order = mainOrderService.newEmptyOrder(login.getUser());
        mainOrderService.supplementOrder(order.getOrderId(), represent.getLogin(), represent.getStore(), storeItem, random.nextInt(5));
        Page orderList = mainOrderService.findAll(new OrderSearcherBody());

    }
}
