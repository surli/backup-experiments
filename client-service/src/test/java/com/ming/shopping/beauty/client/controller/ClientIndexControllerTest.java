package com.ming.shopping.beauty.client.controller;


import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.wx.model.Gender;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
public class ClientIndexControllerTest extends ClientConfigTest {
    private static final String isExistUrl = "/isExist", isRegister = "/isRegister/", sendAuthCode = "/sendAuthCode/";

    @Test
    public void registerOrLogin() throws Exception {
//        //非微信环境
        try {
            mockMvc.perform(get(isExistUrl))
                    .andExpect(status().isOk());
            assertThat(true).isFalse();
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("NoWeixinClientException");
        }

        nextCurrentWechatAccount();

        //没注册时，期望返回空数据
        mockMvc.perform(makeWechat(get(isExistUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(RESULT_CODE_PATH).value(HttpStatusCustom.SC_OK))
                .andExpect(jsonPath(RESULT_DATA_PATH).isEmpty());

        //去注册，先看看这个手机号有没有被注册过
        String mobile = randomMobile();
        mockMvc.perform(makeWechat(get(isRegister + mobile)))
                .andExpect(status().isOk());

        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(mobile);
        registerBody.setAuthCode("1234");
        registerBody.setSurname(randomChinese(1));
        registerBody.setGender(randomEnum(Gender.class));

        //手机号格式不对
        registerBody.setMobile(mobile.substring(1));
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()))
                .andExpect(jsonPath(RESULT_MESSAGE_PATH).value(containsString("手机号")));

        //验证码格式不对
        registerBody.setMobile(mobile);
        registerBody.setAuthCode("12345");
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()))
                .andExpect(jsonPath(RESULT_MESSAGE_PATH).value(containsString("验证码")));

        //没有姓名,期望提示 1002,"注册信息不完成"
        registerBody.setAuthCode("1234");
        registerBody.setSurname(null);
        mockMvc.perform(makeWechat(get(sendAuthCode + registerBody.getMobile())))
                .andExpect(status().isOk());
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andDo(print())
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.MESSAGE_NOT_FULL.getCode()));

        //好了，来个正常的注册
        registerBody.setSurname(randomChinese(1));
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andExpect(status().isOk());

        //再去判断一下是否已经注册了，返回绑定微信的手机号
        mockMvc.perform(makeWechat(get(isExistUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(RESULT_CODE_PATH).value(HttpStatusCustom.SC_OK))
                .andExpect(jsonPath(RESULT_DATA_PATH).value(mobile));

        //去注册，先看看这个手机号有没有被注册过
        mockMvc.perform(makeWechat(get(isRegister + mobile)))
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.MOBILE_EXIST.getCode()));

        //没用注册后的session，此时还不是登录状态，再去执行一下登录
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andExpect(status().isOk());

        //换个方式登录
        //在没登录的情况，随便请求一个接口，期望返回4012
        MockHttpSession mockSession = (MockHttpSession) mockMvc.perform(makeWechat(get("/user")))
                .andExpect(status().is(HttpStatusCustom.SC_NO_OPENID))
                .andReturn().getRequest().getSession();

        //调用 toLogin 接口，期望返回200
        mockSession = (MockHttpSession) mockMvc.perform(makeWechat(get(SystemService.AUTH)
                .session(mockSession)
                .param("redirectUrl",randomHttpURL())))
                .andExpect(status().isFound())
                .andReturn().getRequest().getSession();

        //再次请求 /user，期望返回200
        mockMvc.perform(makeWechat(get("/user").session(mockSession)))
                .andExpect(status().isOk());
    }

    @Test
    public void testWithEmptyLogin() throws Exception {
        nextCurrentWechatAccount();
        //在没登录的情况，随便请求一个接口，期望返回4012
        MockHttpSession mockSession = (MockHttpSession) mockMvc.perform(makeWechat(get("/user")))
                .andExpect(status().is(HttpStatusCustom.SC_NO_OPENID))
                .andReturn().getRequest().getSession();

        //调用 toLogin 接口，期望返回200
        mockSession = (MockHttpSession) mockMvc.perform(makeWechat(get(SystemService.AUTH)
                .session(mockSession)
                .param("redirectUrl",randomHttpURL())))
                .andExpect(status().isFound())
                .andReturn().getRequest().getSession();

        //再次请求 /user，期望返回没有权限
        mockMvc.perform(makeWechat(get("/user").session(mockSession)))
                .andDo(print())
                .andExpect(status().is(HttpStatusCustom.SC_NO_USER));

    }
}