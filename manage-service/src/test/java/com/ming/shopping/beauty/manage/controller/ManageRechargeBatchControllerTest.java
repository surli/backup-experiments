package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import com.ming.shopping.beauty.service.service.SystemService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author CJ
 */
public class ManageRechargeBatchControllerTest extends ManageConfigTest {

    @Autowired
    private RechargeCardRepository rechargeCardRepository;
    @Autowired
    private SystemService systemService;
    @Autowired
    private RechargeCardService rechargeCardService;


    /**
     * 测试生成批量卡片
     */
    @Test
    public void go() throws Exception {
        Integer defaultAmount = systemService.currentCardAmount();

        Login manage = mockRoot();
        updateAllRunWith(manage);
        final long oldTotal = rechargeCardRepository.count();
        final Integer num = 10;
        Login guide = mockGuidableLogin();
        Map<String, Object> data = new HashMap<>();
        data.put("number", num);
        final String email = randomEmailAddress();
        data.put("emailAddress", email);
        data.put("guideId", guide.getId());

        //批量生成充值卡
        mockMvc.perform(post("/rechargeBatch")
                .content(objectMapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        //应该是10个
        assertThat(rechargeCardRepository.count())
                .isEqualTo(oldTotal + num);
        for (RechargeCard r : rechargeCardRepository.findByBatch_EmailAddress(email)) {
            assertThat(r.getAmount()).isEqualTo(BigDecimal.valueOf(defaultAmount));
            assertThat(r.getBatch().getGuideUser().getId()).isEqualTo(guide.getId());
            assertThat(r.getBatch().getManager().getId()).isEqualTo(manage.getId());
            //看下生成的Code
            System.out.println(r.getCode());
        }

        mockMvc.perform(get("/rechargeBatch")).andExpect(status().isOk());

        mockMvc.perform(
                get("/rechargeCard")
        )
                .andExpect(status().isOk());
        // 找一个没用过的
        RechargeCard card = rechargeCardRepository.findAll().stream()
                .filter(rechargeCard -> !rechargeCard.isUsed())
                .min(new RandomComparator()).orElseThrow(IllegalStateException::new);

        // 根据code准确定位
        mockMvc.perform(
                get("/rechargeCard")
                        .param("code", card.getCode())
        )
                .andExpect(jsonPath("$.list.length()").value(1))
                .andExpect(status().isOk());

        // 根据使用用户精准寻找
        Login demoLogin = mockLogin();
        rechargeCardService.useCard(card.getCode(), demoLogin.getId());

        mockMvc.perform(
                get("/rechargeCard")
                        .param("user", demoLogin.getLoginName())
        ).andExpect(jsonPath("$.list.length()").value(1))
                .andExpect(status().isOk());

    }

}