package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.LoginRequest;

/**
 * @author helloztt
 */
public interface LoginRequestService {
    /**
     * 新增登录请求,如果存在则返回
     *
     * @param sessionId
     * @return
     */
    LoginRequest newRequest(String sessionId);

    /**
     * 登录成功
     *
     * @param requestId
     * @param login
     * @return
     */
    LoginRequest login(long requestId, Login login);

    /**
     * 查找信息
     *
     * @param requestId
     * @return
     */
    LoginRequest findOne(long requestId);

    /**
     * 退出登录，删除登录请求
     *
     * @param requestId
     */
    void remove(long requestId);

    /**
     * 退出登录
     *
     * @param login
     */
    void remove(String sessionId, Login login);
}
