package com.ming.shopping.beauty.service.exception;

import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author helloztt
 */
@Data
@AllArgsConstructor
public class ApiResultException extends RuntimeException {
    private int httpStatus;
    private ApiResult apiResult;

    public ApiResultException(int httpStatus){
        this.httpStatus = httpStatus;
    }

    public ApiResultException(ApiResult apiResult){
        this.httpStatus = HttpStatusCustom.SC_DATA_NOT_VALIDATE;
        this.apiResult = apiResult;
    }
}
