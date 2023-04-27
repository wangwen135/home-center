package com.wwh.home.center.common.constant;

/**
 * 返回结果常量
 *
 * @author wangwh
 * @date 2021-3-19
 */
public class ResultConstants {

    /**
     * 成功
     */
    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MSG = "success";

    /**
     * 业务错误
     */
    public static final int BUSINESS_ERROR_CODE = 300;

    /**
     * 错误的请求
     */
    public static final int BAD_REQUEST_CODE = 400;

    /**
     * 请求未授权
     */
    public static final int UNAUTHORIZED_ERROR_CODE = 401;
    public static final String UNAUTHORIZED_ERROR_MSG = "请求未授权";

    /**
     * 参数错误
     */
    public static final int ARGUMENT_ERROR_CODE = 402;
    public static final String ARGUMENT_ERROR_MSG = "参数错误";

    /**
     * 记录不存在
     */
    public static final int RECORD_NOT_EXIST_CODE = 404;
    public static final String RECORD_NOT_EXIST_MSG = "记录不存在";

    /**
     * 服务异常（未知的）
     */
    public static final int SERVER_ERROR_CODE = 500;
    public static final String SERVER_ERROR_MSG = "服务异常";

    /**
     * 系统异常（明确的系统异常）
     */
    public static final int SYSTEM_ERROR_CODE = 501;
    public static final String SYSTEM_ERROR_MSG = "系统异常";

    private ResultConstants() {

    }
}
