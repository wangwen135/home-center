package com.wwh.home.center.common.exception;

import static com.wwh.home.center.common.constant.ResultConstants.UNAUTHORIZED_ERROR_CODE;
import static com.wwh.home.center.common.constant.ResultConstants.UNAUTHORIZED_ERROR_MSG;

/**
 * 权限异常<br>
 * 用于未授权访问系统资源的异常
 *
 * @author wangwh
 * @date 2023/04/27
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super(UNAUTHORIZED_ERROR_CODE, UNAUTHORIZED_ERROR_MSG);
    }

    public UnauthorizedException(String message) {
        super(UNAUTHORIZED_ERROR_CODE, message);
    }
}
