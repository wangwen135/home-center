package com.wwh.home.center.common.exception;

import static com.wwh.home.center.common.constant.ResultConstants.ARGUMENT_ERROR_CODE;
import static com.wwh.home.center.common.constant.ResultConstants.ARGUMENT_ERROR_MSG;

/**
 * 参数异常<br>
 * 如参数校验不通过等
 *
 * @author wangwh
 */
public class ArgumentException extends BaseException {

    public ArgumentException() {
        super(ARGUMENT_ERROR_CODE, ARGUMENT_ERROR_MSG);
    }

    public ArgumentException(String message) {
        super(ARGUMENT_ERROR_CODE, message);
    }

    public ArgumentException(int code, String message) {
        super(code, message);
    }

    public ArgumentException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
