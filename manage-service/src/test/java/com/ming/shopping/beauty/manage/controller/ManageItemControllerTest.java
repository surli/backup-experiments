package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.model.request.NewItemBody;
import com.ming.shopping.beauty.service.repository.ItemRepository;
import lombok.Data;
import org.junit.Test;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageItemControllerTest extends ManageConfigTest {

    final static String BASE_URL = "/item";
    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void itemList() throws Exception {
        //首先是不具有管理员权限的人访问,拒绝访问
        //随便来个人只要不是管理员
        Login fakeManager = mockLogin();
        //身份运行
        updateAllRunWith(fakeManager);
        mockMvc.perform(get("/item"))
                .andDo(print())
                .andExpect(status().isForbidden());
        //root权限
        Login login = mockRoot();

        //添加项目
        //首先需要一个商户
        Merchant merchant = mockMerchant();
        updateAllRunWith(merchant.getLogin());
        Merchant merchant1 = mockMerchant();
        NewItemBody ib = new NewItemBody();
        ib.setName("测试项目名称");
        ib.setMerchantId(merchant.getId());
        ib.setImagePath(randomTmpImagePath());
        ib.setItemType("测试");
        ib.setPrice(BigDecimal.valueOf(188.9));
        ib.setSalesPrice(BigDecimal.valueOf(158.9));
        ib.setCostPrice(BigDecimal.valueOf(158.9));
        ib.setDescription("测试项目");
        ib.setRichDescription("详细的描述了测试项目");

        Item item = mockItem(merchant1);
        //发送添加请求
        mockMvc.perform(post("/item")
                .content(objectMapper.writeValueAsString(ib))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
        //获取项目详情
        String detailJson = mockMvc.perform(get(BASE_URL + "/" + item.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        //获取项目列表
        String contentAsString = mockMvc.perform(get("/item"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].name").value(ib.getName()))
                .andExpect(jsonPath("$.list[1].name").value(item.getName()))
                .andReturn().getResponse().getContentAsString();

        ib.setId(objectMapper.readTree(contentAsString).get("list").get(0).get("id").asLong());
        ib.setName("测试编辑名字");
//        重新给资源
        ib.setImagePath(randomTmpImagePath());
        //编辑
        mockMvc.perform(put("/item")
                .content(objectMapper.writeValueAsString(ib))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
        Item update = itemRepository.getOne(ib.getId());
        assertThat("测试编辑名字".equals(update.getName())).isTrue();


        //获取项目列表
        mockMvc.perform(get("/item"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].name").value(ib.getName()))
                .andExpect(jsonPath("$.list[1].name").value(item.getName()));

        //条件查询
        mockMvc.perform(get("/item")
                .param("name", ib.getName()))
                .andDo(print())
                .andExpect(jsonPath("$.list[0].name").value(ib.getName()));
        //通常都是带有商户id的查询
        mockMvc.perform(get("/item")
                .param("merchantId", String.valueOf(ib.getMerchantId())))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1))
                .andExpect(jsonPath("$.list[0].name").value(ib.getName()));
        //写一个不可能的商户id 获取
        mockMvc.perform(get("/item").param("merchantId", String.valueOf(random.nextInt(1234))))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(0));

        String status = "AUDIT_FAILED";
        //根据项目审核状态去查询
        mockMvc.perform(get("/item")
                .param("auditStatus", "AUDIT_FAILED"))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(0));
        //模拟添加的是通过审核的.发送请求的是没有没提交审核的 这里应该是一个
        mockMvc.perform(get("/item")
                .param("auditStatus", "NOT_SUBMIT"))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1));


        Audit audit = new Audit();
        audit.setStatus("AUDIT_FAILED");
        audit.setComment("没通过");

        //非 root 权限审核
        updateAllRunWith(merchant.getLogin());
        //这里就无所为了.因为没有权限
        mockMvc.perform(put(BASE_URL + "/" + item.getId() + "/auditStatus")
                .content(objectMapper.writeValueAsString(audit))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        //回到merchant权限
        updateAllRunWith(merchant.getLogin());

        audit.setStatus("TO_AUDIT");
        audit.setComment("测试提交审核");
        //提交审核
        mockMvc.perform(put(BASE_URL + "/" + item.getId() + "/commit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(audit)))
                .andDo(print())
                .andExpect(status().isNoContent());

        //回到root 审核
        updateAllRunWith(login);

        item = itemRepository.getOne(item.getId());
        assertThat("TO_AUDIT".equals(item.getAuditStatus().toString())).isTrue();
        assertThat("测试提交审核".equals(item.getAuditComment())).isTrue();

        audit.setStatus("AUDIT_FAILED");
        audit.setComment("没通过");
        //审核不通过
        mockMvc.perform(put(BASE_URL + "/" + item.getId() + "/auditStatus")
                .content(objectMapper.writeValueAsString(audit))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        item = itemRepository.getOne(item.getId());
        assertThat("AUDIT_FAILED".equals(item.getAuditStatus().toString())).isTrue();

        audit.setStatus("TO_AUDIT");
        audit.setComment("测试提交审核");
        //再次提交审核
        mockMvc.perform(put(BASE_URL + "/" + item.getId() + "/commit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(audit)))
                .andDo(print())
                .andExpect(status().isNoContent());

        audit.setStatus("AUDIT_PASS");
        audit.setComment("审核通过");
        //通过
        mockMvc.perform(put(BASE_URL + "/" + item.getId() + "/auditStatus")
                .content(objectMapper.writeValueAsString(audit))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        item = itemRepository.getOne(item.getId());
        assertThat("AUDIT_PASS".equals(item.getAuditStatus().toString())).isTrue();
    }

    @Test
    public void putTest() throws Exception {

        Merchant merchant = mockMerchant();
        updateAllRunWith(merchant.getLogin());

        Item item = mockItem(merchant);
        Item item1 = mockItem(merchant);
        Item item2 = mockItem(merchant);

        //单个上架/下架
        Map<String, Object> putData = new HashMap<>();

        List<Long> items = new ArrayList<>();
        items.add(item.getId());
        putData.put("items", items);
        putData.put("enabled", true);
        mockMvc.perform(put("/itemUpdater/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putData)))
                .andDo(print())
                .andExpect(status().isOk());
        Item one = itemRepository.getOne(item.getId());
        assertThat(one.isEnabled()).isTrue();

        putData.put("enabled", false);
        mockMvc.perform(put("/itemUpdater/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putData)))
                .andDo(print())
                .andExpect(status().isOk());

        one = itemRepository.getOne(item.getId());
        assertThat(one.isEnabled()).isFalse();

        //批量操作 成功三个
        items.add(item1.getId());
        items.add(item2.getId());
        putData.put("enabled", true);
        mockMvc.perform(put("/itemUpdater/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putData)))
                .andDo(print())
                .andExpect(status().isOk());
        //随便那一个应该上架状态
        Item one1 = itemRepository.getOne(item1.getId());
        assertThat(one1.isEnabled()).isTrue();

        putData.put("enabled", false);
        mockMvc.perform(put("/itemUpdater/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putData)))
                .andDo(print())
                .andExpect(status().isOk());

        //随便那一个应该下架状态
        Item one2 = itemRepository.getOne(item2.getId());
        assertThat(one2.isEnabled()).isFalse();


        items.clear();
        items.add(item.getId());
        //推荐/取消推荐
        putData.put("recommended", true);
        mockMvc.perform(put("/itemUpdater/recommended")
                .content(objectMapper.writeValueAsString(putData))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        Item one3 = itemRepository.getOne(item.getId());

        assertThat(one3.isRecommended()).isTrue();

        //取消推荐
        putData.put("recommended", false);
        mockMvc.perform(put("/itemUpdater/recommended")
                .content(objectMapper.writeValueAsString(putData))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        Item one4 = itemRepository.getOne(item.getId());

        assertThat(one4.isRecommended()).isFalse();

        //批量操作
        items.add(item1.getId());
        items.add(item2.getId());
        putData.put("recommended", true);
        mockMvc.perform(put("/itemUpdater/recommended")
                .content(objectMapper.writeValueAsString(putData))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        Item one5 = itemRepository.getOne(item1.getId());
        assertThat(one5.isRecommended()).isTrue();

        putData.put("recommended", false);
        mockMvc.perform(put("/itemUpdater/recommended")
                .content(objectMapper.writeValueAsString(putData))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        Item one6 = itemRepository.getOne(item2.getId());
        assertThat(one6.isRecommended()).isFalse();
    }

    @Test
    public void testList() throws Exception {
        Login root = mockRoot();
        updateAllRunWith(root);
        Merchant merchant = mockMerchant();
        Item item = mockItem(merchant);
        int i = 1;
        while(i < 3){
            i++;
            mockItem(merchant);
        }
        //所有
        mockMvc.perform(get("/item"))
                .andDo(print())
                .andExpect(status().isOk());

        int size = itemRepository.findAll().size();
        //条件
        //还是所有的  一共3个
        mockMvc.perform(get("/item")
                .param("itemName"," ")
                .param("itemType"," ")
                .param("merchantName"," "))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(new GreaterOrEqual(3)));

        //设置一个name条件
        mockMvc.perform(get("/item")
                .param("itemName",item.getName()))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1));


        mockMvc.perform(get("/item")
                .param("merchantName",merchant.getName()))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(3));

    }
    /**
     * 用于测试, 项目审核/状态改变/提交审核
     */
    @Data
    private class Audit {
        private String status;

        private String comment;
    }
}

