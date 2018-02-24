package com.ming.shopping.beauty.controller.process;

import com.ming.shopping.beauty.controller.TogetherTest;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.StagingService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.wx.model.Gender;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.util.Collection;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 注册流程
 * 未注册用户从公众号-个人中心进入：
 * <ol>
 * <li>请求 GET /user: 返回 {@link com.ming.shopping.beauty.service.model.HttpStatusCustom#SC_NO_OPENID} {@code 431}</li>
 * <li>请求 GET /auth 进行授权</li>
 * <li>请求 GET /user: 返回 401 没有权限</li>
 * <li>请求 POST /auth: 返回200 成功</li>
 * <li>请求 GET /user: 返回200（这个时候用户还没被激活）</li>
 * <li>请求 GET /user/vipCard: 返回{@link com.ming.shopping.beauty.service.model.HttpStatusCustom#SC_DATA_NOT_VALIDATE}210，resultCode={@link com.ming.shopping.beauty.service.model.ResultCodeEnum#USER_NOT_ACTIVE} {@code 2005}</li>
 * <li>请求 POST /capital/deposit: 充值成功，跳转到目标页面</li>
 * <li>请求 GET /user/vipCard：返回200</li>
 * <li>新的session</li>
 * <li>请求 GET /user: 返回 {@link com.ming.shopping.beauty.service.model.HttpStatusCustom#SC_NO_OPENID} {@code 431}</li>
 * <li>请求 GET /auth 进行授权</li>
 * <li>请求 GET /user: 返回200</li>
 * </ol>
 *
 * @author helloztt
 */
public class WechatJoinCase extends TogetherTest {
    @Autowired
    private StagingService stagingService;

    @Test
    public void register() throws Exception {
        Object[] registerData = stagingService.registerStagingData();
        //来一个新的微信用户
        nextCurrentWechatAccount();
        MockHttpSession session = (MockHttpSession) mockMvc.perform(wechatGet("/user"))
                .andExpect(status().is(HttpStatusCustom.SC_NO_OPENID))
                .andReturn().getRequest().getSession();

        //授权去,随便搞个跳转地址
        final String userUrl = "/user", vipUrl = "/user/vipCard", deposit = "/capital/deposit";
        mockMvc.perform(wechatGet(SystemService.AUTH)
                .param("redirectUrl", userUrl)
                .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", userUrl));

        mockMvc.perform(wechatGet(userUrl)
                .session(session))
                .andExpect(status().is(HttpStatusCustom.SC_NO_USER));

        //普通注册
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(randomMobile());
        registerBody.setAuthCode("1234");
        registerBody.setSurname(randomChinese(1));
        registerBody.setGender(randomEnum(Gender.class));

        register(registerBody, session);

        mockMvc.perform(wechatGet(userUrl)
                .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(wechatGet(vipUrl)
                .session(session))
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.USER_NOT_ACTIVE.getCode()));

        //充值卡充值
        //找一张没用的充值卡
        Collection<RechargeCard> rechargeCard = (Collection<RechargeCard>) registerData[0];
        String cdKey = rechargeCard.stream().findAny().get().getCode();
        mockMvc.perform(wechatPost(deposit).session(session)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", cdKey)).andDo(print())
                .andExpect(status().isFound());
        //这个时候个人中心有数据了
        mockMvc.perform(wechatGet(vipUrl)
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vipCard").value(cdKey));

        //新的session
        session = (MockHttpSession) mockMvc.perform(wechatGet("/user"))
                .andExpect(status().is(HttpStatusCustom.SC_NO_OPENID))
                .andReturn().getRequest().getSession();
        mockMvc.perform(wechatGet(SystemService.AUTH)
                .param("redirectUrl", userUrl)
                .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", userUrl));
        mockMvc.perform(wechatGet(userUrl)
                .session(session))
                .andExpect(status().isOk());
    }

    private void register(LoginOrRegisterBody registerBody, MockHttpSession session) throws Exception {
        mockMvc.perform(wechatPost(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))
                .session(session))
                .andExpect(status().isOk());
    }
}
