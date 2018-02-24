package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.DepositBody;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author helloztt
 */
public class CapitalControllerTest extends ClientConfigTest {
    private static final String BASE_URL = "/capital";
    public static final String DEPOSIT = BASE_URL + "/deposit";
    private static final String FLOW = BASE_URL + "/flow";

    @Test
    public void testCapitalFlow() {
        // TODO: 2018/1/16 等充值、支付订单都搞好了再做这个
    }

    /**
     * @return 发起支付的结果
     */
    public static ResultActions payOrder(MockMvc mockMvc, MockHttpSession session, long orderId) throws Exception {
        return payOrder(mockMvc, session, orderId, false, null);
    }

    /**
     * @param force     确保人家可以完成支付；若钱不够就充值啦；哈哈
     * @param recharger 负责充值的人
     * @return 发起支付的结果
     */
    public static ResultActions payOrder(MockMvc mockMvc, MockHttpSession session, long orderId, boolean force
            , Callable<?> recharger) throws Exception {
        MockHttpServletRequestBuilder put = MockMvcRequestBuilders.put("/capital/payment/{orderId}", orderId);
        if (session != null)
            put = put.session(session);
        ResultActions actions = mockMvc.perform(put.accept(MediaType.APPLICATION_JSON));
        if (actions.andReturn().getResponse().getStatus() == 402 && force) {
            recharger.call();
            return payOrder(mockMvc, session, orderId, true, recharger);
        }
        return actions;
    }

    /**
     * 对未激活用户充值，期望：激活用户，余额增加，日志增加
     * 对激活用户充值，期望：余额增加，日志增加
     * 1、充值卡充值
     */
    @Test
    public void testDeposit() throws Exception {
        //先创建一个没激活的用户
        Login mockLogin = mockLogin();
        //登录获取session
        MockHttpSession loginSession = login(mockLogin);

        DepositBody postData = new DepositBody();
        //1、格式错误
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains( "code=" + ResultCodeEnum.NO_MONEY_CARD.getCode()
                        + "&msg=")));
        postData.setDepositSum(BigDecimal.ONE);
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("depositSum", postData.getDepositSum().toString()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("code=" + ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getCode()
                        + "&msg=")));
        postData.setDepositSum(null);
        postData.setCdKey("123");
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", postData.getCdKey()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("code=" + ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        + "&msg=")));
        //2、错误的充值卡
        postData.setCdKey(String.format("%20d", 0));
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", postData.getCdKey()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("code=" + ResultCodeEnum.CARD_NOT_EXIST.getCode()
                        + "&msg=")));
        //3、正确的充值卡
        BigDecimal current = loginService.findBalance(mockLogin.getUser().getId());
        RechargeCard rechargeCard = mockRechargeCard();
        postData.setCdKey(rechargeCard.getCode());
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", postData.getCdKey()))
                .andExpect(status().isFound());
        mockLogin = loginService.findOne(mockLogin.getId());
        assertThat(mockLogin.getUser().isActive()).isTrue();
        assertThat(loginService.findBalance(mockLogin.getUser().getId()).subtract(current))
                .as("充入准确的金额")
                .isCloseTo(rechargeCard.getAmount(), Offset.offset(new BigDecimal("0.000001")));
        //4、已被使用的充值卡
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", postData.getCdKey()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("code=" + ResultCodeEnum.CARD_ALREADY_USED.getCode()
                        + "&msg=")));
        //5、查询流水
        mockMvc.perform(get(FLOW)
                .session(loginSession))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1))
                .andExpect(jsonPath("$.list[0].sum").value(rechargeCard.getAmount().doubleValue()));
    }
}