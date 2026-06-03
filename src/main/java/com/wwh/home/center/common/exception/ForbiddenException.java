package com.wwh.home.center.common.exception;

import static com.wwh.home.center.common.constant.ResultConstants.FORBIDDEN_ERROR_CODE;
import static com.wwh.home.center.common.constant.ResultConstants.FORBIDDEN_ERROR_MSG;

/**
 * 权限异常<br>
 * 用于未授权访问系统资源的异常
 *
 * @author wangwh
 * @date 2024/02/06
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super(FORBIDDEN_ERROR_CODE, FORBIDDEN_ERROR_MSG);
    }

    public ForbiddenException(String message) {
        super(FORBIDDEN_ERROR_CODE, message);
    }
}
