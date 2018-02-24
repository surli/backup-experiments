package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.model.request.NewStoreBody;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.RepresentRepository;
import com.ming.shopping.beauty.service.repository.StoreRepository;
import com.ming.shopping.beauty.service.service.StoreService;
import org.junit.Test;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageStoreControllerTest extends ManageConfigTest {

    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private StoreService storeService;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private RepresentRepository representRepository;

    private static final String BASE_URL = "/store";

    @Test
    public void goList() throws Exception {
        Login root = mockRoot();
        updateAllRunWith(root);
        Merchant merchant = mockMerchant();
        Merchant merchant2 = mockMerchant();
        Store store = mockStore(merchant);
        mockStore(merchant2);

        int size = storeRepository.findAll().size();

        mockMvc.perform(get("/store"))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(size));

        mockMvc.perform(get("/store")
                .param("username",store.getLogin().getLoginName()))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1));

    }
    @Test
    public void storeList() throws Exception {
        //首先是不具有管理员权限的人访问,拒绝访问
        //随便来个人只要不是管理员
        Login fackManage = mockLogin();
        //身份运行
        updateAllRunWith(fackManage);
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
        //来个商户
        Merchant merchant = mockMerchant();
        //用这个商户来运行
        updateAllRunWith(merchant.getLogin());
        //将要成为门店的用户
        Login willStore = mockLogin();


        //添加一个门店
        NewStoreBody rsb = new NewStoreBody();
        rsb.setContact("王女士");
        rsb.setName("测试的门店");
        rsb.setTelephone("18799882273");
        rsb.setLoginId(willStore.getId());
        rsb.setMerchantId(merchant.getId());
        System.out.println(objectMapper.writeValueAsString(rsb));
        //发送请求添加
        String location = mockMvc.perform(post(BASE_URL)
                .content(objectMapper.writeValueAsString(rsb))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getHeader("Location");
        Store store = storeService.findStore(willStore.getId());
        assertThat(store != null).isTrue();
        //再添加一个
        Store store1 = mockStore(merchant);
        //不带参数，获取门店列表,第一条记录必定是最新添加的
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total").value(new GreaterOrEqual<>(2)))
                .andExpect(jsonPath("$.list[0].id").value(store1.getId()))
                .andExpect(jsonPath("$.list[1].id").value(rsb.getLoginId()));
        mockMvc.perform(get(BASE_URL)
                .param("merchantId", String.valueOf(merchant.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total").value(new GreaterOrEqual<>(2)))
                .andExpect(jsonPath("$.list[0].id").value(store1.getId()))
                .andExpect(jsonPath("$.list[1].id").value(rsb.getLoginId()));

        //查找特定门店
        mockMvc.perform(get(BASE_URL)
                .param("username",willStore.getLoginName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total").value(1))
                .andExpect(jsonPath("$.list[0].id").value(rsb.getLoginId()));

        //找一个不存在的门店
        mockMvc.perform(get(BASE_URL)
                .param("merchantId",String.valueOf(100000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total").value(0))
                .andExpect(jsonPath("$.list").isEmpty());

        boolean enable = false;
        //禁用门店
        mockMvc.perform(put(BASE_URL + "/" + store.getId() + "/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enable)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Store findOne = storeRepository.getOne(store.getId());
        assertThat(findOne.isEnabled()).isFalse();

    }

    @Test
    public void representTest() throws Exception {
        //来个商户
        Merchant merchant = mockMerchant();
        //用这个商户来运行
        updateAllRunWith(merchant.getLogin());
        //添加一个门店
        Store store = mockStore(merchant);
        //为这个门店添加两个门店代表
        //第一个模拟添加
        Represent represent = mockRepresent(store);
        //第二个发送请求

        //先生成一个用户
        Login login = mockLogin();
        mockMvc.perform(post("/store/" + store.getId() + "/represent/" + login.getId()))
                .andDo(print())
                .andExpect(status().isCreated());
        //获取门店代表列表
        String contentAsString = mockMvc.perform(get("/store/" + store.getId() + "/represent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        //现在看看是否有这两个门店代表
        long rId1 = objectMapper.readTree(contentAsString).get("list").get(0).get("id").asLong();
        long rId2 = objectMapper.readTree(contentAsString).get("list").get(1).get("id").asLong();

        assertThat(Arrays.asList(login.getId(), represent.getId()).containsAll(Arrays.asList(rId1, rId2))).isTrue();


        boolean enable = false;
        //冻结启用门店代表
        mockMvc.perform(put("/store/" + store.getId() + "/represent/" + login.getId() + "/enabled")
                .content(objectMapper.writeValueAsString(false))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        //查看他是否禁用
        Represent one = representRepository.getOne(login.getId());
        assertThat(one.isEnable()).isFalse();

        //接触角色与门店代表关联
        mockMvc.perform(delete("/store/" + store.getId() + "/represent/" + login.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());
        Login nowLogin = loginRepository.getOne(login.getId());
        assertThat(nowLogin.getRepresent() == null).isTrue();
        Store nowStore = storeRepository.getOne(store.getId());
        assertThat(nowStore.getRepresents().contains(one)).isFalse();
    }

}
