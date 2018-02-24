package com.ming.shopping.beauty.service.model;


import org.apache.http.HttpStatus;

/**
 * 用来定义 HttpStatus 拓展的响应码,整理一下主要有以下几种情况:
 * <p>请求成功：返回{@link HttpStatusCustom#SC_OK} 200</p>
 * <p>已接收请求：返回{@link HttpStatusCustom#SC_ACCEPTED} 202</p>
 * <p>已接受请求，但没有返回内容：返回{@link HttpStatusCustom#SC_NO_CONTENT} 204</p>
 * <p>请求不合规：返回{@link HttpStatusCustom#SC_DATA_NOT_VALIDATE} 210</p>
 * <p>未登录：返回{@link HttpStatusCustom#SC_UNAUTHORIZED} 401</p>
 * <p>SESSION过期：返回{@link HttpStatusCustom#SC_SESSION_TIMEOUT} 430</p>
 * <p>未授权：返回{@link HttpStatusCustom#SC_NO_OPENID} 431</p>
 * <p>未注册：返回{@link HttpStatusCustom#SC_NO_USER} 432</p>
 * <p>不允许操作：返回{@link HttpStatusCustom#SC_FORBIDDEN} 403</p>
 * <p>用户不存在：返回{@link HttpStatusCustom#SC_LOGIN_NOT_EXIST}  433</p>
 * <p>系统正在维护：返回{@link HttpStatusCustom#SC_SERVICE_UNAVAILABLE} 503</p>
 * <p>系统故障：返回{@link HttpStatusCustom#SC_INTERNAL_SERVER_ERROR} 500</p>
 * <p></p>
 *
 * @author helloztt
 * @date 2018/1/4
 */
public interface HttpStatusCustom extends HttpStatus {
    /**
     * {@code 210 请求不合规}
     */
    int SC_DATA_NOT_VALIDATE = 210;
    /**
     * {@code 430 session过期}
     */
    int SC_SESSION_TIMEOUT = 430;
    /**
     * {@code 431 微信未授权}
     */
    int SC_NO_OPENID = 431;
    /**
     * {@code 432 未注册}
     */
    int SC_NO_USER = 432;
    /**
     * 用户不存在(先在不会有没有Login的情况了）
     */
    @Deprecated
    int SC_LOGIN_NOT_EXIST = 433;

    /**
     * {@code 508 系统维护中}
     */
    int SC_SYSTEM_MAINTENANCE = 508;
}
