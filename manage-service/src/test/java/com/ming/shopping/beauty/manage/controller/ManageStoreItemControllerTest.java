package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.request.NewStoreItemBody;
import com.ming.shopping.beauty.service.repository.ItemRepository;
import com.ming.shopping.beauty.service.repository.StoreItemRepository;
import me.jiangcai.lib.test.matcher.NumberMatcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author
 */
public class ManageStoreItemControllerTest extends ManageConfigTest {
    @Autowired
    private StoreItemRepository storeItemRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void goTest() throws Exception {
        Login root = mockRoot();
        updateAllRunWith(root);

        Merchant merchant = mockMerchant();
        Store store = mockStore(merchant);
        Item item = mockItem(merchant);

        StoreItem storeItem = mockStoreItem(store, item);
        mockStoreItem(mockStore(merchant), mockItem(merchant));
        mockStoreItem(mockStore(merchant), mockItem(merchant));
        int size = storeItemRepository.findAll().size();

        mockMvc.perform(get("/storeItem")
                .param("storeName"," ")
                .param("itemName"," "))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(size));


        mockMvc.perform(get("/storeItem")
                .param("itemName",item.getName()))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1));


        mockMvc.perform(get("/storeItem")
                .param("storeName",store.getName()))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1));



    }
    @Test
    public void listTest() throws Exception {
        //创建一个商户,以他来运行
        Merchant merchant = mockMerchant();
        updateAllRunWith(merchant.getLogin());
        //创建一个门店
        Store store = mockStore(merchant);
        //创建一个项目
        Item item = mockItem(merchant);

        //以门店运行吧
        updateAllRunWith(store.getLogin());


        item.setAuditStatus(AuditStatus.NOT_SUBMIT);
        itemRepository.save(item);

        //添加到门店项目
        NewStoreItemBody nsi = new NewStoreItemBody();
        nsi.setItemId(item.getId());
        nsi.setStoreId(store.getId());
        nsi.setSalesPrice(item.getSalesPrice().setScale(2).subtract(BigDecimal.valueOf(0.10)));
        //添加到门店项目
        //因为项目没通过审核,所以不能添加
        mockMvc.perform(post("/storeItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nsi)))
                .andDo(print())
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE));
        //价格设置不对,添加失败
        mockMvc.perform(post("/storeItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nsi)))
                .andDo(print())
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE));

        //设置正确的
        item.setAuditStatus(AuditStatus.AUDIT_PASS);
        itemRepository.save(item);
        nsi.setSalesPrice(item.getPrice().setScale(2).multiply(BigDecimal.valueOf(0.95)));
        mockMvc.perform(post("/storeItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nsi)))
                .andDo(print())
                .andExpect(status().isCreated());


        //再添加一个
        NewStoreItemBody nsi1 = new NewStoreItemBody();
        Item item1 = mockItem(merchant);
        nsi1.setItemId(item1.getId());
        nsi1.setStoreId(store.getId());
        nsi1.setSalesPrice(item1.getPrice().setScale(2).multiply(BigDecimal.valueOf(0.95)));
        mockMvc.perform(post("/storeItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nsi1)))
                .andDo(print())
                .andExpect(status().isCreated());

        System.out.println(nsi.getSalesPrice() + "-------------------------------------------------------------" + nsi1.getSalesPrice());

        //查看列表
        mockMvc.perform(get("/storeItem"))
                .andDo(print())
                .andExpect(jsonPath("$.list[0].salesPrice").value(NumberMatcher.numberAsDoubleEquals(nsi1.getSalesPrice())))
                .andExpect(jsonPath("$.list[1].salesPrice").value(NumberMatcher.numberAsDoubleEquals(nsi.getSalesPrice())))
                .andExpect(status().isOk());
        //根据特别数据获取
        mockMvc.perform(get("/storeItem")
                .param("itemName", String.valueOf(item.getName())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].name").value(item.getName()));

        List<StoreItem> all = storeItemRepository.findAll();

        //修改门店项目的销售价 不合理的范围内, 小于 item的销售价
        BigDecimal salesPrice = all.get(0).getSalesPrice();
        mockMvc.perform(put("/storeItem/" + all.get(0).getId())
                .content(objectMapper.writeValueAsString(salesPrice.subtract(new BigDecimal(100))))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE));

        //修改门店项目的销售价 合理的范围内
        mockMvc.perform(put("/storeItem/" + all.get(0).getId())
                .content(objectMapper.writeValueAsString(salesPrice.add(new BigDecimal(0.1))))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }


    @Test
    public void enableGo() throws Exception {
        final String enabled = "enabled";
        final String storeItems = "storeItems";
        final String recommended = "recommended";
        //创建一个商户,以他来运行
        Merchant merchant = mockMerchant();
        updateAllRunWith(merchant.getLogin());
        //创建一个门店
        Store store = mockStore(merchant);
        //创建一个项目
        Item item = mockItem(merchant);
        Item item1 = mockItem(merchant);

        //为门店添加了3个门店项目
        StoreItem storeItem = mockStoreItem(store, item);
        StoreItem storeItem1 = mockStoreItem(store, item);
        StoreItem storeItem2 = mockStoreItem(store, item1);

        Map<String, Object> map = new HashMap<>();
        Long[] longs = new Long[3];
        longs[0] = storeItem.getId();
        longs[1] = storeItem1.getId();
        longs[2] = storeItem2.getId();
        map.put(enabled, true);
        map.put(storeItems, longs);
        //将他们批量上架
        mockMvc.perform(put("/storeItemUpdater/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk());
        for (int i = 0; i < longs.length; i++) {
            StoreItem one = storeItemRepository.getOne(longs[i]);
            assertThat(one.isEnable()).isTrue();
        }
        //将它们批量下架
        map.put(enabled, false);
        mockMvc.perform(put("/storeItemUpdater/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk());
        for (int i = 0; i < longs.length; i++) {
            StoreItem one = storeItemRepository.getOne(longs[i]);
            assertThat(one.isEnable()).isFalse();
        }

        map.remove("enabled");
        map.put(recommended,true);
        //批量推荐
        mockMvc.perform(put("/storeItemUpdater/recommended")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk());
        for (int i = 0; i < longs.length; i++) {
            StoreItem one = storeItemRepository.getOne(longs[i]);
            assertThat(one.isRecommended()).isTrue();
        }

        map.remove("enabled");
        map.put(recommended,false);
        //批量取消推荐
        mockMvc.perform(put("/storeItemUpdater/recommended")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk());
        for (int i = 0; i < longs.length; i++) {
            StoreItem one = storeItemRepository.getOne(longs[i]);
            assertThat(one.isRecommended()).isFalse();
        }
    }

    @Test
    public void go()throws Exception{
        final String param = "enabled";
        final String items = "items";
        //下架一个item时,storeItem也应该下架
        //创建一个商户,以他来运行
        Merchant merchant = mockMerchant();
        updateAllRunWith(merchant.getLogin());
        //创建一个门店
        Store store = mockStore(merchant);
        //创建一个项目
        Item item = mockItem(merchant);
        //添加两个门店项目
        StoreItem storeItem = mockStoreItem(store, item);
        StoreItem storeItem1 = mockStoreItem(store, item);

        StoreItem one = storeItemRepository.getOne(storeItem.getId());
        StoreItem two = storeItemRepository.getOne(storeItem1.getId());
        one.setEnable(true);
        two.setEnable(true);
        storeItemRepository.save(one);
        storeItemRepository.save(two);

        Map<String, Object> map = new HashMap<>();
        Long[] longs = new Long[1];
        longs[0] = item.getId();
        map.put(param, false);
        map.put(items, longs);

        mockMvc.perform(put("/itemUpdater/enabled")
                .content(objectMapper.writeValueAsString(map))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        List<StoreItem> byItem = storeItemRepository.findByItem(item);
        if(byItem.size() == 1){
            throw new RuntimeException("storeItem的数量不对");
        }
        for (StoreItem s : byItem) {
            assertThat(s.isEnable()).isFalse();
        }
    }

}
