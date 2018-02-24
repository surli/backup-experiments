package com.ming.shopping.beauty.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.definition.MainOrderModel;
import com.ming.shopping.beauty.service.model.request.ItemNum;
import com.ming.shopping.beauty.service.model.request.NewOrderBody;
import com.ming.shopping.beauty.service.model.request.OrderSearcherBody;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ClientMainOrderControllerTest extends ClientConfigTest {
    public static ResultActions makeOrderFor(MockMvc mockMvc, MockHttpSession representSession
            , Map<StoreItem, Integer> randomMap, long orderId) throws Exception {
        NewOrderBody orderBody = new NewOrderBody();
        orderBody.setOrderId(orderId);
        // 现实情况是 我们通过/items 响应出去的id 是Item的id 而非StoreItem的id 所以这里必须调整为这样
        ItemNum[] items = randomMap.keySet().stream().map(p -> new ItemNum(p.getItem().getId()
                , randomMap.get(p))).toArray(ItemNum[]::new);
        orderBody.setItems(items);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/order");
        if (representSession != null)
            post = post.session(representSession);
        return mockMvc.perform(post
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(orderBody)));
    }

    @Test
    public void go() throws Exception {
        //先看看没数据的订单长啥样
        OrderSearcherBody searcherBody = new OrderSearcherBody();
        String response = mockMvc.perform(get("/orders")
                .session(activeUserSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searcherBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int total = objectMapper.readTree(response).get("pagination").get("total").asInt();

        //造订单啦~
        //首先要有 商户，门店，项目，门店项目
        Merchant mockMerchant = mockMerchant();
        Store mockStore = mockStore(mockMerchant);
        for (int i = 0; i < 5; i++) {
            Item item = mockItem(mockMerchant);
            mockStoreItem(mockStore, item);
        }

        //然后需要有下单的用户(mockActiveUser)和门店代表
        Represent mockRepresent = mockRepresent(mockStore);
        //以门店身份登录
        MockHttpSession representSession = login(mockRepresent.getLogin());

        //来它至少2个订单
        int randomOrderNum = 2 + random.nextInt(5);
        for (int i = 0; i < randomOrderNum; i++) {
            //先获取用户的 orderId
            response = mockMvc.perform(get("/user/vipCard")
                    .session(activeUserSession))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            long orderId = objectMapper.readTree(response).get("orderId").asLong();
            makeOrderFor(mockMvc, representSession, randomOrderItemSet(mockStore.getId()), orderId)
                    .andExpect(status().isOk());
            MainOrder mainOrder = mainOrderService.findById(orderId);
            assertThat(mainOrder.getOrderStatus().toString()).isEqualTo(OrderStatus.forPay.toString());
            assertThat(mainOrder.getOrderItemList()).isNotEmpty();
            //再次下单会提示错误
            makeOrderFor(mockMvc, representSession, randomOrderItemSet(mockStore.getId()), orderId)
                    .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                    .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.ORDER_NOT_EMPTY.getCode()));
        }


        //激活的用户获取订单列表
        response = mockMvc.perform(get("/orders")
                .session(activeUserSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searcherBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").value(matchModels(new MainOrderModel(mainOrderService))))
                .andReturn().getResponse().getContentAsString();
        int totalOrderNum = objectMapper.readTree(response).get("pagination").get("total").asInt();
        assertThat(totalOrderNum).isEqualTo(total + randomOrderNum);
        //获取第一个订单编号
        JsonNode orderList = objectMapper.readTree(response).get("list");
        for (JsonNode order : orderList) {
            long orderId = order.get("orderId").asLong();
            //根据这个订单编号查
            mockMvc.perform(get("/orders/" + orderId)
                    .session(activeUserSession))
                    .andExpect(jsonPath("$.orderId").value(orderId));
        }
    }

    @Test
    public void mainOrderDetailTest() throws Exception {
        Login login = mockLogin();
        Login root = mockRoot();
        updateAllRunWith(root);

        Merchant merchant = mockMerchant();
        Store store = mockStore(merchant);
        Represent represent = mockRepresent(store);
        Item item = mockItem(merchant);
        StoreItem storeItem = mockStoreItem(store, item);

        Map<StoreItem,Integer> requestItem = new HashMap<>();
        requestItem.put(storeItem,3);
        MainOrder mainOrder = mockMainOrder(login.getUser(), represent, requestItem);

        //查看详情

        mockMvc.perform(get("/orders/{orderId}",mainOrder.getOrderId()))
                .andDo(print())
                .andExpect(jsonPath("$").value(matchModel(new MainOrderModel(mainOrderService))))
                .andExpect(status().isOk());


    }
}

