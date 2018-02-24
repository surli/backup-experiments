package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.CapitalService;
import com.ming.shopping.beauty.service.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author lxf
 */
@Controller
@PreAuthorize("hasAnyRole('ROOT')")
public class ManageMoneyController {

    @Autowired
    private CapitalService capitalService;
    @Autowired
    private LoginService loginService;

    /**
     * 管理员手动给某个账户充值
     *
     * @param login  充值操作的管理元
     * @param postData 请求数据
     */
    @PostMapping("/manage/manualRecharge")
    @ResponseBody
    @Transactional
    public ApiResult manualRecharge(@AuthenticationPrincipal Login login, @RequestBody Map<String,Object> postData) {
        final String amount = "amount";
        final String mobile = "mobile";

        if(postData.get(amount) == null){
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getCode(),
                    MessageFormat.format(ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getMessage(), amount), null));
        }
        if(postData.get(mobile) == null){
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.CARD_FAILURE.getCode(),
                    MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), mobile), null));
        }

        BigDecimal rechargeAmount = new BigDecimal(postData.get(amount).toString());
        String telephone = postData.get(mobile).toString();

        if (rechargeAmount.compareTo(BigDecimal.ZERO) != 1) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getCode(),
                    MessageFormat.format(ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getMessage(), rechargeAmount), null));
        }
        if (StringUtils.isEmpty(telephone)) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.CARD_FAILURE.getCode(),
                    MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), telephone), null));
        }

        Login one = loginService.findOne(telephone);
        User user = one.getUser();
        try {
            capitalService.manualRecharge(login, user,rechargeAmount);
        } catch (Exception e) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.CARD_FAILURE.getCode(),
                    ResultCodeEnum.CARD_FAILURE.getMessage(), null));
        }
        return ApiResult.withOk("充值金额:" + rechargeAmount + ",手机号码:" + telephone + ",充值成功");
    }

    /**
     * 扣款/提现操作
     * @param login  操作员
     * @param login  充值操作的管理元
     * @param postData 请求数据
     * @return
     */
    @PostMapping("/manage/deduction")
    @Transactional
    @ResponseBody
    public ApiResult deduction(@AuthenticationPrincipal Login login, @RequestBody Map<String,Object> postData) {
        final String amount = "amount";
        final String mobile = "mobile";

        if(postData.get(mobile) == null){
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.CARD_FAILURE.getCode(),
                    MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), mobile), null));
        }
        String telephone = postData.get(mobile).toString();
        if (StringUtils.isEmpty(telephone)) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode(),
                    MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), mobile), null));
        }
        if (postData.get(amount) != null && new BigDecimal(postData.get(amount).toString()).compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode(),
                    MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), amount), null));
        }
        if(postData.get(amount) == null){
            //清空余额
            Login one = loginService.findOne(telephone);
            User user = one.getUser();
            BigDecimal deduction = null;
            try {
                deduction = capitalService.deduction(login, user, null);
            } catch (Exception e) {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.DEDUCTION_FAILURE.getCode(),
                        ResultCodeEnum.DEDUCTION_FAILURE.getMessage(), null));
            }
            return ApiResult.withOk("扣款金额:" + deduction + ",手机号码:" + telephone + ",扣款成功");
        }else{
            //按金额扣款
            Login one = loginService.findOne(telephone);
            User user = one.getUser();
            BigDecimal subAmount = new BigDecimal(postData.get(amount).toString());
            try {
                capitalService.deduction(login, user, subAmount);
            } catch (Exception e) {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.DEDUCTION_FAILURE.getCode(),
                        ResultCodeEnum.DEDUCTION_FAILURE.getMessage(), null));
            }
            return ApiResult.withOk("扣款金额:" + subAmount + ",手机号码:" + telephone + ",扣款成功");
        }

    }


}
