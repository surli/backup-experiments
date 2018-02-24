package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageLoginTest extends ManageConfigTest {

    @Autowired
    private LoginRepository loginRepository;

    @Test
    public void go() throws Exception {
        //用户列表
        Login login = mockRoot();
        updateAllRunWith(login);
        //生成5个用户
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ids.add(mockLogin().getId());
        }

        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk());
        //List<Login> list = objectMapper.readValue(String.valueOf(objectMapper.readTree(contentAsString).findPath("list"))
        // , new TypeReference<List<Login>>() {});
        //条件查询
        mockMvc.perform(get("/login")
                .param("loginId", String.valueOf(login.getId())))
                .andDo(print())
                .andExpect(status().isOk());
        //用户详情

        mockMvc.perform(get("/login/"+ids.get(0)))
                .andDo(print())
                .andExpect(status().isOk());
        //用户的启用禁用
        boolean putData = false;
        mockMvc.perform(put("/login/"+ids.get(0)+"/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putData)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Login one = loginRepository.findOne(ids.get(0));
        assertThat(one.isEnabled()).isFalse();
    }
}
