package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.utils.Constant;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 系统服务；它不依赖任何玩意儿
 *
 * @author helloztt
 */
public interface SystemService {
    /**
     * 一些请求地址
     */
    String LOGIN = "/auth";
    String LOGIN_OUT = "/logout";
    String TO_LOGIN = "/toLogin";
    String AUTH = "/auth";

    /**
     * @return 当前充值卡面额
     */
    Integer currentCardAmount();

    /**
     * @param uri 传入uri通常/开头
     * @return 完整路径
     * @deprecated 应该准确地说明用户场景，比如桌面版或者移动版
     */
    String toUrl(String uri);

    String toMobileUrl(String uri);

    String toDesktopUrl(String uri);

    /**
     * @param orderId 订单号
     * @return 前端打开这个订单的页面
     */
    default String toMobileShopUrl(Object orderId) {
        return toMobileUrl("/#/shop/" + orderId);
    }

    /**
     * @return 前端首页
     */
    default String toMobileHomeUrl() {
        return toMobileUrl("/#/personal");
    }

    /**
     * @return 前端注册页面
     */
    default String toMobileJoinUrl() {
        return toMobileUrl("/#/join");
    }

    /**
     * @return 前端会员卡
     */
    default String toMobileVipUrl() {
        return toMobileUrl("/#/vip");
    }

    /**
     * @return 项目列表
     */
    default String toMobileItemUrl() {
        return toMobileUrl("/#/items");
    }

    /**
     * @param errorMsg 错误信息
     * @return 错误页面
     * @throws UnsupportedEncodingException
     */
    default String toErrorUrl(Integer code, String errorMsg) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(errorMsg)) {
            errorMsg = "请求错误";
        }
        StringBuilder sb = new StringBuilder("/#/result?");
        //前端用不到这个code ,仅为了方便单元测试
        if (code != null) {
            sb.append("code=").append(code).append("&");
        }
        sb.append("msg=").append(URLEncoder.encode(errorMsg, Constant.UTF8_ENCODIND));
        return toMobileUrl(sb.toString());
    }
}
