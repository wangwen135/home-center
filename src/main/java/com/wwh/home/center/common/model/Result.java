package com.wwh.home.center.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

import static com.wwh.home.center.common.constant.ResultConstants.*;

/**
 * 返回结果对象
 *
 * @param <T>
 * @author wangwh
 * @date 2021-3-19
 */
@ApiModel("返回结果对象")
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("返回码 （200=成功；30X=业务错误；40X=错误的请求，参数错误等；500=服务异常）")
    private int code;

    @ApiModelProperty("返回消息")
    private String message;

    @ApiModelProperty("时间戳")
    private String timestamp;

    @ApiModelProperty("数据")
    private T data;

    public Result() {
        timestamp = LocalDateTime.now().toString();
    }

    public Result(int code, String message) {
        timestamp = LocalDateTime.now().toString();
        this.code = code;
        this.message = message;
    }

    public Result(int code, String message, T data) {
        timestamp = LocalDateTime.now().toString();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MSG);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg);
    }

    public static <T> Result<T> error(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>(UNAUTHORIZED_ERROR_CODE, UNAUTHORIZED_ERROR_MSG);
    }

    public static <T> Result<T> badRequest(String msg, T t) {
        return new Result<>(BAD_REQUEST_CODE, msg, t);
    }

    public static <T> Result<T> notExist() {
        return new Result<>(RECORD_NOT_EXIST_CODE, RECORD_NOT_EXIST_MSG);
    }

    public static <T> Result<T> serverError() {
        return new Result<>(SERVER_ERROR_CODE, SERVER_ERROR_MSG);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
