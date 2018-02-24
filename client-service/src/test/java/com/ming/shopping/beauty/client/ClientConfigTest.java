package com.ming.shopping.beauty.client;

import com.ming.shopping.beauty.client.config.ClientConfig;
import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.wx.model.Gender;
import me.jiangcai.wx.model.WeixinUserDetail;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
@ContextConfiguration(classes = ClientConfig.class)
public abstract class ClientConfigTest extends CoreServiceTest {
    @Autowired
    private RechargeCardService rechargeCardService;

    private WeixinUserDetail mockWeixinUser;
    /**
     * 未激活的用户
     */
    protected Login mockUnActiveUser;
    protected MockHttpSession unActiveUserSession;

    /**
     * 已激活的用户
     */
    protected Login mockActiveUser;
    protected MockHttpSession activeUserSession;

    @Before
    public void setupByClient() throws Exception {
        LoginOrRegisterBody unActiveUser = new LoginOrRegisterBody()
                , activeUser = new LoginOrRegisterBody();
        unActiveUser.setMobile(randomMobile());
        unActiveUser.setAuthCode("1234");
        unActiveUser.setSurname(randomChinese(1) );
        unActiveUser.setGender(randomEnum(Gender.class));
        unActiveUserSession = register(unActiveUser);
        mockUnActiveUser = loginService.findOne(unActiveUser.getMobile());

        activeUser.setMobile(randomMobile());
        activeUser.setAuthCode("1234");
        activeUser.setSurname(randomChinese(1) );
        activeUser.setGender(randomEnum(Gender.class));
        activeUser.setCdKey(mockRechargeCard().getCode());
        activeUserSession = register(activeUser);
        mockActiveUser = loginService.findOne(activeUser.getMobile());


    }

    protected MockHttpSession register(LoginOrRegisterBody registerBody) throws Exception {
        mockWeixinUser = nextCurrentWechatAccount();
        mockMvc.perform(makeWechat(get("/sendAuthCode/" + registerBody.getMobile())))
                .andExpect(status().isOk());
        return  (MockHttpSession)mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }

    protected MockHttpSession login(Login login) throws Exception{
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(login.getLoginName());
        registerBody.setAuthCode("1234");
        nextCurrentWechatAccount(login.getWechatUser());
        mockMvc.perform(makeWechat(get("/sendAuthCode/" + login.getLoginName())))
                .andExpect(status().isOk());
        return  (MockHttpSession)mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }
}
