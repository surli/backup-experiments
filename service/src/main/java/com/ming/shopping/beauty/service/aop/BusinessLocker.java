package com.ming.shopping.beauty.service.aop;

/**
 * 支持成为一个业务锁
 * Created by helloztt on 2017-01-09.
 */
public interface BusinessLocker {

    /**
     * @return 业务锁
     */
    Object toLock();
}
