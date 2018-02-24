package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.repository.RechargeOrderRepository;
import com.ming.shopping.beauty.service.utils.Constant;
import me.jiangcai.payment.PayableOrder;
import me.jiangcai.payment.entity.PayOrder;
import me.jiangcai.payment.service.PayableSystemService;
import me.jiangcai.wx.pay.entity.WeixinPayOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author helloztt
 */
@Service
public class WeixinPayService implements PayableSystemService {
    private static final Log log = LogFactory.getLog(WeixinPayService.class);
    @Autowired
    private SystemService systemService;
    @Autowired
    private RechargeOrderRepository rechargeOrderRepository;

    @Override
    public ModelAndView paySuccess(HttpServletRequest request, PayableOrder payableOrder, PayOrder payOrder) {
        if (payOrder instanceof WeixinPayOrder) {
            return new ModelAndView("redirect:" + ((WeixinPayOrder) payOrder).getRedirectUrl());
        } else {
            throw new IllegalStateException("暂时不支持：" + payableOrder);
        }
    }

    @Override
    public ModelAndView pay(HttpServletRequest request, PayableOrder order, PayOrder payOrder, Map<String, Object> additionalParameters) {
        ModelAndView modelAndView = new ModelAndView();
        if (payOrder instanceof WeixinPayOrder) {
            WeixinPayOrder weixinPayOrder = (WeixinPayOrder) payOrder;
            weixinPayOrder.setRedirectUrl(additionalParameters.get("redirectUrl").toString());
            modelAndView.setViewName("/views/paying");
            modelAndView.addObject("payRequestParam", weixinPayOrder.getJavascriptToPay());
            modelAndView.addObject("successRedirectUrl", systemService.toMobileUrl(additionalParameters.get("successUri").toString()));
            modelAndView.addObject("failureRedirectUrl", systemService.toMobileUrl("/error?status=400&message=充值失败"));
            log.debug("js for pay:" + weixinPayOrder.getJavascriptToPay()
                    + ",successUri:" + modelAndView.getModelMap().get("successRedirectUrl")
                    + ",failureUri:" + modelAndView.getModelMap().get("failureRedirectUrl")
                    + "redirectUrl:" + weixinPayOrder.getRedirectUrl());
        }
        return modelAndView;
    }

    @Override
    public boolean isPaySuccess(String id) {
        return false;
    }

    @Override
    public PayableOrder getOrder(String id) {
        Long orderId = payableOrderIdToId(id);
        if (orderId != null) {
            return rechargeOrderRepository.findOne(orderId);
        }
        throw new IllegalArgumentException("不支持的可支付ID:" + id);
    }

    public static Long payableOrderIdToId(String str) {
        if (StringUtils.isEmpty(str))
            return null;
        if (!str.contains("-"))
            return null;
        return NumberUtils.parseNumber(str.substring(str.indexOf("-") + 1), Long.class);
    }
}
