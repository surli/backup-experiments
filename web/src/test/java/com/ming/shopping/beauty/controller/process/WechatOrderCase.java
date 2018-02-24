package com.ming.shopping.beauty.controller.process;

import com.jayway.jsonpath.JsonPath;
import com.ming.shopping.beauty.client.controller.CapitalControllerTest;
import com.ming.shopping.beauty.client.controller.ClientItemControllerTest;
import com.ming.shopping.beauty.client.controller.ClientMainOrderControllerTest;
import com.ming.shopping.beauty.controller.TogetherTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.model.request.DepositBody;
import com.ming.shopping.beauty.service.service.StagingService;
import com.ming.shopping.beauty.service.service.StoreItemService;
import me.jiangcai.lib.test.matcher.SimpleMatcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.ming.shopping.beauty.client.controller.CapitalControllerTest.DEPOSIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 微信下单case
 * <ol>
 * <li>构建商户，门店，项目以及门店项目；还有门店代表 应该在main代码中，因为staging也需要自己完成这么一份数据</li>
 * <li>用户访问/items 可以获得门店项目；而不是项目，应该校验结果是否仅仅在项目中或者未被激活 还有格式检测</li>
 * <li>用户访问/users/vipCard 可以获得订单号；以及被扫码的地址</li>
 * <li>门店代表 可以根据自己的 storeId 访问/items 获取他们自己的门店项目</li>
 * <li>门店代表 可以通过post /order 完成下单</li>
 * <li>用户 可以通过 PUT /capital/payment/{orderId} 完成支付</li>
 * <li>门店代表 可以通过 get /orders 获得该用户已支付的信息</li>
 * </ol>
 * 门店代表可以完成的工作，店主也可以完成！
 *
 * @author CJ
 */
public class WechatOrderCase extends TogetherTest {

    @Autowired
    private StagingService stagingService;
    @Autowired
    private StoreItemService storeItemService;

    @Test
    public void flow() throws Exception {
        // 1
        Object[] generatingData = stagingService.generateStagingData();
        Store store = (Store) generatingData[1];
        Represent represent = (Represent) generatingData[2];
        Item[] items = (Item[]) generatingData[3];
        Item okItem = items[0];
        Item[] otherItems = new Item[items.length - 1];
        System.arraycopy(items, 1, otherItems, 0, otherItems.length);

        Login user = mockLogin();
        updateAllRunWith(user);
        mockMvc.perform(
                get("/items")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(ClientItemControllerTest.resultMatcherForItems())
                // item 是一个Collection 必须拥有第一个item 必须不可以拥有其他item
                .andExpect(jsonPath("$.list[*].title").value(new SimpleMatcher<Collection<String>>(
                        names -> {
                            assertThat(names)
                                    .as("必须包含期待的")
                                    .contains(okItem.getName())
                                    .as("必须不包含不期待的")
                                    .doesNotContain(Stream.of(otherItems).map(Item::getName).toArray(String[]::new));
                            return true;
                        }
                )))
        ;
        // 从第三到最后，门店代表和店主都可以走
        fromThreeToEnd(okItem, user, represent.getLogin(), represent.getStore().getId());
        fromThreeToEnd(okItem, user, store.getLogin(), store.getId());
    }

    /**
     * 从第三步到最后
     *
     * @param okItem          准备购买的项目
     * @param user            用户
     * @param storeAboutLogin 门店相关登录
     * @param storeId         门店id
     * @throws Exception
     */
    private void fromThreeToEnd(Item okItem, Login user, Login storeAboutLogin, Long storeId) throws Exception {
        // 第三
//        先充值
        recharge();
        Number orderId = JsonPath.read(mockMvc.perform(
                get("/user/vipCard")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.qrCode").isString())
                .andReturn().getResponse().getContentAsString(), "$.orderId");

        // 第四

        updateAllRunWith(storeAboutLogin);

        mockMvc.perform(
                get("/items")
                        .param("storeId", storeId.toString())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(ClientItemControllerTest.resultMatcherForItems())
                // item 是一个Collection 必须拥有第一个item 必须不可以拥有其他item
                .andExpect(jsonPath("$.list[*].title").value(new SimpleMatcher<Collection<String>>(
                        names -> {
                            assertThat(names)
                                    .as("只包含特定门店的")
                                    .containsOnly(okItem.getName());
                            return true;
                        }
                )))
        ;
        // 第五 开始下单
        // 我们得获取具体的StoreItem，基于维持测试代码稳定性的需求
        Map<StoreItem, Integer> toBuy = new HashMap<>();
        toBuy.put(fromItem(okItem), 1);
        ClientMainOrderControllerTest.makeOrderFor(mockMvc, null, toBuy, orderId.longValue())
                .andExpect(status().isOk());

        // 第六 完成支付，确保完成支付，要是没钱那么就给他钱
        updateAllRunWith(user);
        CapitalControllerTest.payOrder(mockMvc, null, orderId.longValue(), true, () -> {
            recharge();
            return null;
        })
                .andExpect(status().is2xxSuccessful());

        // 第七

        updateAllRunWith(storeAboutLogin);
        mockMvc.perform(
                get("/orders").param("orderType", "STORE")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$.list[0].orderStatusCode").value(2));

        updateAllRunWith(user);
        mockMvc.perform(
                get("/orders").param("orderType", "MEMBER")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$.list[0].orderStatusCode").value(2));
    }

    private StoreItem fromItem(Item item) {
        return storeItemService.findByItem(item).get(0);
    }

    private void recharge() throws Exception {
        RechargeCard rechargeCard = mockRechargeCard();
        DepositBody postData = new DepositBody();
        postData.setCdKey(rechargeCard.getCode());
        mockMvc.perform(post(DEPOSIT)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", postData.getCdKey()))
                .andExpect(status().isFound());
    }

}
