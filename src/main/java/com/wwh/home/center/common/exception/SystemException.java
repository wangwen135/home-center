package com.wwh.home.center.common.exception;

import static com.wwh.home.center.common.constant.ResultConstants.*;

/**
 * 系统异常<br>
 * 用于系统级别的异常，如数据库连接失败等。
 *
 * @author wangwh
 * @date 2023/04/27
 */
public class SystemException extends BaseException {
    public SystemException(String message) {
        super(SYSTEM_ERROR_CODE, message);
    }

    public SystemException(int code, String message) {
        super(code, message);
    }

    public SystemException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
