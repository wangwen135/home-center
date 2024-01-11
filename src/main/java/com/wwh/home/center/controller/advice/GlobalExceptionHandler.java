package com.wwh.home.center.controller.advice;


import com.wwh.home.center.common.constant.ResultConstants;
import com.wwh.home.center.common.exception.*;
import com.wwh.home.center.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 *
 * @author wangwh
 * @date 2021-3-19
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.info("方法参数错误，请求地址：{}", request.getRequestURI(), e);
        return Result.error(ResultConstants.ARGUMENT_ERROR_CODE, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.info("请求方法不支持，请求地址：{}", request.getRequestURI(), e);
        return Result.badRequest("请求方法不支持");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.info("请求内容不可读，请求地址：{}", request.getRequestURI(), e);
        return Result.badRequest("错误的请求，内容不可读");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.info("缺少请求参数，请求地址：{}", request.getRequestURI(), e);
        return Result.badRequest("缺少请求参数：" + e.getMessage());
    }

    /**
     * 表单绑定到 java bean 出错时抛出
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({BindException.class})
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected Result<?> handleBindException(BindException ex, HttpServletRequest request) {
        log.info("表单绑定到 java bean 异常，请求地址：{}", request.getRequestURI(), ex);
        Map<String, String> errors = getErrorMap(ex.getBindingResult());
        return Result.badRequest("参数错误", errors);
    }

    /**
     * 请求体绑定到java bean上失败时抛出
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.info("请求体绑定到java bean 异常，请求地址：{}", request.getRequestURI(), ex);
        Map<String, String> errors = getErrorMap(ex.getBindingResult());
        return Result.badRequest("参数错误", errors);
    }

    private Map<String, String> getErrorMap(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * 普通参数(非 java bean)校验出错时抛出<br>
     * 使用：@Validated 注解
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({ConstraintViolationException.class})
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<?> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        log.info("普通参数校验异常，请求地址：{}", request.getRequestURI(), ex);
        return Result.badRequest(ex.getMessage(), null);
    }

    /**
     * 参数类型不匹配
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.info("参数类型不匹配，请求地址：{}", request.getRequestURI(), ex);
        return Result.badRequest(ex.getMessage(), null);
    }

    /**
     * 请求未授权
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({UnauthorizedException.class})
    @ResponseBody
    public Result<?> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        log.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        log.info("未授权异常：code={} msg={}", ex.getCode(), ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 参数异常 处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({ArgumentException.class})
    @ResponseBody
    public Result<?> handleArgumentException(ArgumentException ex, HttpServletRequest request) {
        log.debug("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        log.info("参数异常：code={} msg={}", ex.getCode(), ex.getMessage(), ex);
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 业务异常 处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({BusinessException.class})
    @ResponseBody
    public Result<?> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        log.warn("业务异常：code={} msg={}", ex.getCode(), ex.getMessage(), ex);
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 系统异常 处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({SystemException.class})
    @ResponseBody
    public Result<?> handleSystemException(SystemException ex, HttpServletRequest request) {
        log.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        log.error("系统异常：code={} msg={}", ex.getCode(), ex.getMessage(), ex);
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 基础异常处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({BaseException.class})
    @ResponseBody
    public Result<?> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());
        log.error("基础异常：code={} msg={}", ex.getCode(), ex.getMessage(), ex);
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 全局异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        log.error("出现异常", e);
        return Result.serverError();
    }
}
