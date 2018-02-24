package com.ming.shopping.beauty.manage.controller;

import com.jayway.jsonpath.JsonPath;
import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.definition.ManagerModel;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.InitService;
import com.ming.shopping.beauty.service.service.SystemService;
import org.junit.Test;
import org.mockito.internal.matchers.StartsWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author helloztt
 */
public class ManageControllerTest extends ManageConfigTest {
    @Autowired
    private ConversionService conversionService;
    private static final String manageLogin = "/managerLogin", managerLoginRequest = "/managerLoginRequest", manageLoginResult = "/manageLoginResult";

    @Test
    public void goList() throws Exception {
        Login root = mockRoot();
        mockManager(ManageLevel.rootItemManager);
        updateAllRunWith(root);

        int total = JsonPath.read(mockMvc.perform(get("/manage"))
                .andReturn().getResponse().getContentAsString(), "$.pagination.total");

        // 增加了一个
        mockRoot();
        mockMvc.perform(get("/manage"))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(total + 1));

        //默认的一个. 我添加了两个 共1个
        mockMvc.perform(get("/manage")
                .param("username", root.getLoginName()))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1));

    }

    //
//    /**
//     * 非微信环境无法工作
//     */
//    @Test(expected = NoWeixinClientException.class)
//    public void weixinOnly() throws Throwable {
//        try {
//            mockMvc.perform(get(manageLogin + "/12345"))
//                    .andExpect(status().isOk());
//        } catch (NestedServletException nestedServletException) {
//            throw nestedServletException.getCause();
//        }
//    }

    /**
     * 管理员后台扫码的登录过程中有2个参与session
     * 一个session是PC那边的，另一头则是微信这边的
     * 分别取名desktopSession,wechatSession
     */
    @Test
    public void testManageIndex() throws Exception {
        //以CJ的超级管理员账号为测试对象
        nextCurrentWechatAccount();


        //随便找个ID登录，期望提示session失效
        MockHttpSession wechatSession = (MockHttpSession) mockMvc.perform(wechatGet(manageLogin + "/" + random.nextInt(1000)))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new StartsWith("/auth")))
                .andReturn().getRequest().getSession();

        // 桌面开始搞事情了
        MockHttpSession desktopSession = (MockHttpSession) mockMvc.perform(
                get("/currentManager")
        )
                .andExpect(status().isForbidden())
                .andReturn().getRequest().getSession();
        //去获取一个ID
        MvcResult mvcResult = mockMvc.perform(get(managerLoginRequest)
                .session(desktopSession)
        )
                .andExpect(status().is(HttpStatusCustom.SC_ACCEPTED))
                .andReturn();
        //之后判断登录状态需要用这个session
//        MockHttpSession desktopSession = (MockHttpSession) mvcResult.getRequest().getSession();
        String result = mvcResult.getResponse().getContentAsString();
        String requestId = objectMapper.readTree(result).get("id").asText();
        //再次登录，由于CJ的账号没有openId，期望 HttpStatusCustom.SC_LOGIN_NOT_EXIST
        mockMvc.perform(wechatGet(manageLogin + "/" + requestId).session(wechatSession))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new StartsWith("/auth")));

        //用蒋才的账号登录一下，更新openId
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(InitService.cjMobile);
        registerBody.setAuthCode("1234");
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody)))
                .session(wechatSession)
        )
                .andExpect(status().isOk());

        //登录前看看登录情况
        mockMvc.perform(get(manageLoginResult + "/" + requestId)
                .session(desktopSession))
                .andExpect(status().isNoContent());

        //再次登录，期望成功
        mockMvc.perform(wechatGet(manageLogin + "/" + requestId)
                .session(wechatSession)
        )
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new StartsWith("http")));

        //看看登录结果
        mockMvc.perform(get(manageLoginResult + "/" + requestId)
                .session(desktopSession))
                .andExpect(status().isOk());

        // 若桌面再次发起登录请求，应该直接给数据，而且协议符合ManagerModal
        mockMvc.perform(
                get(managerLoginRequest)
                        .session(desktopSession)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(matchModel(new ManagerModel(conversionService))))
        ;

        // 同时，应当支持 /currentManager
        mockMvc.perform(
                get("/currentManager")
                        .session(desktopSession)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$").value(matchModel(new ManagerModel(conversionService))))
        ;

        //再试一个没有权限的用户
        Login login = mockLogin();
        mvcResult = mockMvc.perform(wechatGet(managerLoginRequest))
                .andExpect(status().is(HttpStatusCustom.SC_ACCEPTED))
                .andReturn();
        desktopSession = (MockHttpSession) mvcResult.getRequest().getSession();
        result = mvcResult.getResponse().getContentAsString();
        requestId = objectMapper.readTree(result).get("id").asText();
        nextCurrentWechatAccount(login.getWechatUser());

        mockMvc.perform(get(manageLoginResult + "/" + requestId).session(desktopSession)).andExpect(status().isNoContent());

        mockMvc.perform(wechatGet(manageLogin + "/" + requestId))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new StartsWith("/auth")));

        mockMvc.perform(get(manageLoginResult + "/" + requestId).session(desktopSession)).andExpect(status().isNoContent());
    }

    /**
     * 测试管理流程
     */
    @Test
    public void manageIt() throws Exception {
        Login root = mockRoot();
        updateAllRunWith(root);
        // 查下管理员的数量
        int total = JsonPath.read(mockMvc.perform(
                get("/manage")
        ).andReturn().getResponse().getContentAsString(), "$.pagination.total");
        // 添加一个普通用户之后管理员数量保持不变
        mockLogin();
        mockMvc.perform(
                get("/manage")
        ).andExpect(jsonPath("$.pagination.total").value(total));
        // 添加一个商户之后管理员数量保持不变
        mockMerchant();
        mockMvc.perform(
                get("/manage")
        ).andExpect(jsonPath("$.pagination.total").value(total));
        // 但如果添加的是一个管理员 那就不行了
        int count = 0;
        for (ManageLevel level : Login.rootLevel) {
            count++;
            final int current = total + count;
            mockManager(level);
            mockMvc.perform(
                    get("/manage")
            ).andExpect(jsonPath("$.pagination.total").value(current));
        }

        // 测试更新权限
        Login one = mockLogin();
        assertThat(one.getLevelSet())
                .as("一开始是没权限的")
                .hasSameElementsAs(Arrays.asList(ManageLevel.user));

        // 确保没有root
        ManageLevel[] targetLevel = null;
        while (targetLevel == null || Arrays.binarySearch(targetLevel, ManageLevel.root) >= 0) {
            targetLevel = randomArray(Login.rootLevel.toArray(new ManageLevel[Login.rootLevel.size()]), 1);
        }

        System.out.println(objectMapper.writeValueAsBytes(Stream.of(targetLevel).map(Enum::name).collect(Collectors.toList())));

        //
        mockMvc.perform(
                put("/manage/{id}/levelSet", one.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Stream.of(targetLevel).map(Enum::name).collect(Collectors.toList())))
        )
                .andExpect(status().is2xxSuccessful());
        //因为我们知道mockLogin具有user权限,所以给他加上
        ManageLevel[] containUserLevel = new ManageLevel[targetLevel.length + 1];
        System.arraycopy(targetLevel, 0, containUserLevel, 0, targetLevel.length);
        containUserLevel[targetLevel.length] = ManageLevel.user;

        assertThat(loginService.findOne(one.getId()).getLevelSet())
                .as("新的权限符合需求")
                .hasSameElementsAs(Arrays.asList(containUserLevel));

        // 再清楚掉
        mockMvc.perform(
                put("/manage/{id}/levelSet", one.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new ArrayList<>()))
        )
                .andExpect(status().is2xxSuccessful());
        // 他应该具有user权限
        assertThat(loginService.findOne(one.getId()).getLevelSet())
                .as("应该没有权限了")
                .hasSameElementsAs(Arrays.asList(ManageLevel.user));

    }
}