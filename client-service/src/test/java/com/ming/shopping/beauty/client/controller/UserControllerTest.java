package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import me.jiangcai.lib.test.matcher.NumberMatcher;
import org.junit.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
public class UserControllerTest extends ClientConfigTest {
    private static final String BASE_URL = "/user";

    @Test
    public void userBaseInfo() throws Exception {
        //未登录的情况下请求，跳转到未登录接口
        mockMvc.perform(makeWechat(get(BASE_URL)))
                .andExpect(status().is(HttpStatusCustom.SC_NO_OPENID));

        //未激活用户的数据
        mockMvc.perform(get(BASE_URL)
                .session(unActiveUserSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0D))
                .andExpect(jsonPath("$.storeId").value(""));

        //激活用户的数据
        mockMvc.perform(get(BASE_URL)
                .session(activeUserSession))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(NumberMatcher.numberGreatThan(0)));

        final String vipCardUrl = BASE_URL + "/vipCard";
        //未激活用户请求会员卡
        mockMvc.perform(get(vipCardUrl)
                .session(unActiveUserSession))
                .andDo(print())
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.USER_NOT_ACTIVE.getCode()));

        //激活用户请求会员卡
        mockMvc.perform(get(vipCardUrl).session(activeUserSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").isNotEmpty());

    }

}