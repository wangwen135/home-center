package com.wwh.home.center.common.exception;

/**
 * 异常基类
 *
 * @author wangwh
 * @date 2023/04/27
 */
public class BaseException extends RuntimeException {
    private int code;

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
