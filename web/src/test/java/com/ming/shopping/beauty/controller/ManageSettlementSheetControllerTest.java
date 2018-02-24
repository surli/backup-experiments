package com.ming.shopping.beauty.controller;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import com.ming.shopping.beauty.service.entity.support.SettlementStatus;
import com.ming.shopping.beauty.service.model.request.DepositBody;
import com.ming.shopping.beauty.service.model.request.SheetReviewBody;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.repository.settlementSheet.SettlementSheetRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ManageSettlementSheetControllerTest extends TogetherTest {

    @Autowired
    private LoginService loginService;
    @Autowired
    private MainOrderRepository mainOrderRepository;
    @Autowired
    private RechargeCardRepository rechargeCardRepository;
    @Autowired
    private SettlementSheetRepository settlementSheetRepository;

    @Test
    public void go() throws Exception {
        //下单者
        Login login = mockLogin();
        //商户
        Merchant merchant = mockMerchant();
        //门店
        Store store = mockStore(merchant);
        for (int i = 0; i < 5; i++) {
            Item item = mockItem(merchant);
            mockStoreItem(store, item);
        }
        //门店代表
        Represent mockRepresent = mockRepresent(store);
        //以门店身份登录
        updateAllRunWith(mockRepresent.getLogin());

        //创建订单
        MainOrder mainOrder = mockMainOrder(login.getUser(), mockRepresent);

        //支付订单
//        /payment/{orderId}

        //余额是0的情况下
        mockMvc.perform(put("/capital/payment/{orderId}", mainOrder.getOrderId())
                .content(objectMapper.writeValueAsString(mainOrder.getFinalAmount()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        //root运行
        Login root = mockRoot();
        updateAllRunWith(root);
        //生成充值卡
        final RechargeCard rechargeCard = mockRechargeCard(mockRepresent.getLogin());
        //充值卡充值
        //以充值人运行
        BigDecimal balance = getCurrentBalance(merchant.getLogin(), login);

        updateAllRunWith(login);
        DepositBody depositBody = new DepositBody();
        depositBody.setCdKey(rechargeCard.getCode());
        mockMvc.perform(post("/capital/deposit")
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", depositBody.getCdKey()))
                .andDo(print())
                .andExpect(status().isFound());

        final BigDecimal currentBalance2 = getCurrentBalance(merchant.getLogin(), login);
        assertThat(currentBalance2)
                .as("充值成功 不给加钱；你丫找死么。")
                .isGreaterThan(balance);

//        updateAllRunWith(merchant.getLogin());

        //重新支付订单ok了
        mockMvc.perform(put("/capital/payment/{orderId}", mainOrder.getOrderId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        //看看他的余额
        //总金额5000, 支付金额 mainOrder.getFinalAmount()
        assertThat(getCurrentBalance(merchant.getLogin(), login))
                .as("花钱就是牙签")
                .isEqualTo(currentBalance2.subtract(mainOrder.getFinalAmount()));

        //作弊将这个订单完成时间换成一周之前.好生成结算单
        MainOrder newMainOrder = mainOrderRepository.findOne(mainOrder.getOrderId());
        newMainOrder.setPayTime(LocalDateTime.now().minusDays(8));
        mainOrderRepository.save(newMainOrder);

        //再来一单
        MainOrder mainOrderTwo = mockMainOrder(login.getUser(), mockRepresent);
        mockMvc.perform(put("/capital/payment/{orderId}", mainOrderTwo.getOrderId())
                .content(objectMapper.writeValueAsString(mainOrderTwo.getFinalAmount()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
        
        MainOrder newMainOrderTwo = mainOrderRepository.findOne(mainOrderTwo.getOrderId());
        newMainOrderTwo.setPayTime(LocalDateTime.now().minusDays(8));
        mainOrderRepository.save(newMainOrderTwo);
        
        //生成结算单
        mockMvc.perform(post("/settlementSheet/{merchantId}",merchant.getId()))
                .andExpect(status().isCreated()).andReturn().getResponse();

        //应该只有一个结算单
        List<SettlementSheet> all = settlementSheetRepository.findAll();
        assertThat(all.size()).isEqualTo(1);
        SettlementSheet settlementSheet = all.get(0);

        updateAllRunWith(root);
        //查看结算单列表
        String contentAsString2 = mockMvc.perform(get("/settlementSheet"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String sumAmount = objectMapper.readTree(contentAsString2).get("list").get(0).get("settlementAmount").asText();
        BigDecimal bSumAmount = new BigDecimal(sumAmount);
        //偷个懒
        assertThat(bSumAmount.setScale(1))
                .isEqualTo(mainOrder.getSettlementAmount().add(mainOrderTwo.getSettlementAmount()).setScale(1));

        //商户将结算单提交
        SheetReviewBody srb = new SheetReviewBody();
        srb.setComment("我是一个备注");
        srb.setStatus("TO_AUDIT");

        updateAllRunWith(merchant.getLogin());
        mockMvc.perform(put("/settlementSheet/{id}/statusMerchant",settlementSheet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(srb)))
                .andDo(print())
                .andExpect(status().isNoContent());

        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getComment()).isEqualTo("我是一个备注");
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.TO_AUDIT);


        updateAllRunWith(root);
        //打回  "打回申请的备注"
        srb.setComment("打回申请的备注");
        srb.setStatus("REJECT");
        mockMvc.perform(put("/settlementSheet/{id}/statusManage",settlementSheet.getId())
                .content(objectMapper.writeValueAsString(srb))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getComment()).isEqualTo("打回申请的备注");
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.REJECT);


        updateAllRunWith(merchant.getLogin());
        //再次提交  "我是一个备注"
        srb.setComment("我是一个备注");
        srb.setStatus("TO_AUDIT");
        mockMvc.perform(put("/settlementSheet/{id}/statusMerchant",settlementSheet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(srb)))
                .andExpect(status().isNoContent());

        //同意申请  "同意申请"
        srb.setComment("同意申请");
        srb.setStatus("APPROVAL");
        updateAllRunWith(root);
        mockMvc.perform(put("/settlementSheet/{id}/statusManage",settlementSheet.getId())
                .content(objectMapper.writeValueAsString(srb))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.APPROVAL);

        //线下打款了

        //修改状态以打款
        srb.setStatus("ALREADY_PAID");
        srb.setAmount(mainOrder.getSettlementAmount().add(mainOrderTwo.getSettlementAmount()));
        mockMvc.perform(put("/settlementSheet/{id}/statusManage",settlementSheet.getId())
                .content(objectMapper.writeValueAsString(srb))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.ALREADY_PAID);

        //确认收款
        updateAllRunWith(merchant.getLogin());
        srb.setStatus("COMPLETE");
        //商户确认接收
        mockMvc.perform(put("/settlementSheet/{id}/statusMerchant",settlementSheet.getId())
                .content(objectMapper.writeValueAsString(srb))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETE);
    }

    private BigDecimal getCurrentBalance(Login watcher, Login login) throws Exception {
        updateAllRunWith(watcher);
        return new BigDecimal(mockMvc.perform(get("/login/{id}/balance", login.getUser().getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }
}
