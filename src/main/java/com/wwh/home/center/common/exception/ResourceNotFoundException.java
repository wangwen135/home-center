package com.wwh.home.center.common.exception;

/**
 * 资源未找到
 *
 * @author wangwh
 * @date 2024/01/29
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
}
