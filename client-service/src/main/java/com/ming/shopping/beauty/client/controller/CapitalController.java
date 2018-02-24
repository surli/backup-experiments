package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.log.CapitalFlow;
import com.ming.shopping.beauty.service.entity.log.CapitalFlow_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User_;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.RechargeOrder;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.DepositBody;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import com.ming.shopping.beauty.service.service.RechargeOrderService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.lib.sys.service.SystemStringService;
import me.jiangcai.payment.exception.SystemMaintainException;
import me.jiangcai.payment.service.PaymentService;
import me.jiangcai.wx.pay.service.WeixinPaymentForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author helloztt
 */
@Controller
@RequestMapping("/capital")
public class CapitalController {
    @Autowired
    private ConversionService conversionService;
    @Autowired
    private RechargeCardService rechargeCardService;
    @Autowired
    private RechargeOrderService rechargeOrderService;
    @Autowired
    private SystemStringService systemStringService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private WeixinPaymentForm weixinPaymentForm;
    @Autowired
    private LoginService loginService;
    @Autowired
    private MainOrderService mainOrderService;
    @Autowired
    private Environment env;

    @GetMapping("/flow")
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<CapitalFlow> capitalFlow(@AuthenticationPrincipal Login login) {
        return new RowDefinition<CapitalFlow>() {
            @Override
            public Class<CapitalFlow> entityClass() {
                return CapitalFlow.class;
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<CapitalFlow> root) {
                return Arrays.asList(
                        criteriaBuilder.desc(root.get(CapitalFlow_.id))
                );
            }

            @Override
            public List<FieldDefinition<CapitalFlow>> fields() {
                return listFields();
            }

            @Override
            public Specification<CapitalFlow> specification() {
                return (root, cq, cb) ->
                        cb.equal(root.join(CapitalFlow_.user).get(User_.id), login.getId());
            }
        };
    }

    @Autowired
    private SystemService systemService;

    @PostMapping(value = "/deposit", produces = "text/html")
    public ModelAndView deposit(@AuthenticationPrincipal Login login, @Valid DepositBody postData
            , BindingResult bindingResult, HttpServletRequest request) throws SystemMaintainException {
        if (bindingResult.hasErrors()) {
            throw new ApiResultException(
                    //提示 XXX格式错误
                    ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                            , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage()
                                    , bindingResult.getAllErrors().get(0).getDefaultMessage())
                            , null));
        }

        if (postData.getDepositSum() != null) {
            Double minAmount = systemStringService.getCustomSystemString("shopping.service.recharge.min.amount"
                    , null, true, Double.class
                    , env.acceptsProfiles("staging") ? 0D : 5000D);
            if (postData.getDepositSum().compareTo(BigDecimal.valueOf(minAmount)) < 0) {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getCode()
                        , MessageFormat.format(ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getMessage(), minAmount.toString()), null));
            }
            //先新增一个支付订单
            RechargeOrder rechargeOrder = rechargeOrderService.newOrder(login.getUser(), postData.getDepositSum());
            //走支付流程
            Map<String, Object> additionalParam = new HashMap<>(1);
            additionalParam.put("redirectUrl", postData.getRedirectUrl());
            additionalParam.put("openId", login.getWechatUser().getOpenId());
            return paymentService.startPay(request, rechargeOrder, weixinPaymentForm, additionalParam);
        } else if (!StringUtils.isEmpty(postData.getCdKey())) {
            //使用充值卡
            rechargeCardService.useCard(postData.getCdKey(), login.getId());
            return toResult(postData);
        } else {
            throw new ApiResultException((ApiResult.withError(ResultCodeEnum.NO_MONEY_CARD)));
        }
    }

    private ModelAndView toResult(DepositBody postData) {
        ModelAndView model = new ModelAndView();
        final String s = "paySuccess=true";
        if (postData.getRedirectUrl() == null) {
            model.setViewName("redirect:" + systemService.toMobileHomeUrl() + "?" + s);
            return model;
        }
        final String redirectUrl = postData.getRedirectUrl();
        if (StringUtils.isEmpty(s)) {
            model.setViewName("redirect:" + redirectUrl);
        } else {
            if (redirectUrl.contains("?"))
                model.setViewName("redirect:" + redirectUrl + "&" + s);
            else
                model.setViewName("redirect:" + redirectUrl + "?" + s);
        }

        return model;
    }

    /**
     * 支付订单
     *
     * @param orderId 支付的订单id
     */
    @PutMapping("/payment/{orderId}")
    public ResponseEntity payOrder(@PathVariable(value = "orderId") long orderId) {
        //获取待支付的这个订单
        MainOrder mainOrder = mainOrderService.findById(orderId);
        BigDecimal amount = mainOrder.getFinalAmount();
        //查看用户的余额
        BigDecimal balance = loginService.findBalance(mainOrder.getPayer().getId());
        ///判断余额是否足够支付订单不够返回差额
        if (balance.compareTo(amount) < 0) {
            //差值
            return ResponseEntity
                    .status(402)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(amount.subtract(balance));
        } else {
            //支付成功
            mainOrderService.payOrder(orderId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
    }

    private List<FieldDefinition<CapitalFlow>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(CapitalFlow.class, "time")
                        .addSelect(root -> root.get(CapitalFlow_.happenTime))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "title")
                        .addSelect(root -> root.get(CapitalFlow_.flowType))
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "sum")
                        .addSelect(root -> root.get(CapitalFlow_.changed))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "type")
                        .addSelect(root -> root.get(CapitalFlow_.flowType))
                        .addFormat((data, type) -> ((Enum) data).ordinal())
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "orderId")
                        .addSelect(root -> root.get(CapitalFlow_.id))
                        .build()
        );
    }
}
