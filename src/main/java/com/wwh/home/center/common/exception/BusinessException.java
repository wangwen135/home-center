package com.wwh.home.center.common.exception;

import static com.wwh.home.center.common.constant.ResultConstants.BUSINESS_ERROR_CODE;

/**
 * 业务异常<br>
 * 用于业务逻辑出现的异常，如校验不通过等
 *
 * @author wangwh
 * @date 2023/04/27
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(BUSINESS_ERROR_CODE, message);
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
