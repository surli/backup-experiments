package com.ming.shopping.beauty.controller.process;

import com.ming.shopping.beauty.controller.TogetherTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.service.SystemService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
//@ActiveProfiles("emulation")
public class ManageRechargeCardControllerTest extends TogetherTest {

    @Autowired
    private SystemService systemService;

    /**
     * 测试生成批量卡片
     */
    @Test
    public void go() throws Exception {
        Integer defaultAmount = systemService.currentCardAmount();

        Login manage = mockRoot();
        updateAllRunWith(manage);
        final Integer num = 10;
        Login guide = mockGuidableLogin();
        Map<String, Object> data = new HashMap<>();
        data.put("number", num);
        data.put("emailAddress", "caijiang@mingshz.com");
        data.put("guideId", guide.getId());

        //批量生成充值卡
        mockMvc.perform(post("/rechargeBatch")
                .content(objectMapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
