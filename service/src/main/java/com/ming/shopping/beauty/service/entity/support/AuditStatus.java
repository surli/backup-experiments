package com.ming.shopping.beauty.service.entity.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态
 *
 * @author helloztt
 */
@Getter
@AllArgsConstructor
public enum AuditStatus {
    NOT_SUBMIT("待提交"),
    TO_AUDIT("待审核"),
    AUDIT_PASS("审核通过"),
    AUDIT_FAILED("审核不通过");

    private String message;
}
