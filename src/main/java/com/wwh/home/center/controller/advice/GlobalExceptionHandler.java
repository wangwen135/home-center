package com.wwh.home.center.controller.advice;


import com.wwh.home.center.common.exception.ArgumentException;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.common.exception.SystemException;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.common.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private HttpServletRequest request;

    /**
     * 表单绑定到 java bean 出错时抛出
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({BindException.class})
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected Result<?> handleBindException(BindException ex) {
        logger.warn("表单绑定到 java bean 异常", ex);
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
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("请求体绑定到java bean 异常", ex);
        Map<String, String> errors = getErrorMap(ex.getBindingResult());
        return Result.badRequest("参数错误", errors);
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
    public Result<?> handleConstraintViolationException(ConstraintViolationException ex) {
        logger.warn("普通参数校验异常", ex);
        return Result.badRequest(ex.getMessage(), null);
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
     * 参数类型不匹配
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        logger.warn("参数类型不匹配", ex);
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
    public Result<?> handleUnauthorizedException(UnauthorizedException ex) {
        logger.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        logger.info("未授权异常：code={} msg={}", ex.getCode(), ex.getMessage());
        return Result.unauthorized();
    }

    /**
     * 参数异常 处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({ArgumentException.class})
    @ResponseBody
    public Result<?> handleArgumentException(ArgumentException ex) {
        logger.debug("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        logger.info("参数异常：code={} msg={}", ex.getCode(), ex.getMessage(), ex);
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
    public Result<?> handleBusinessException(BusinessException ex) {
        logger.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        logger.warn("业务异常：code={} msg={}", ex.getCode(), ex.getMessage(), ex);
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
    public Result<?> handleSystemException(SystemException ex) {
        logger.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        logger.error("系统异常：code={} msg={}", ex.getCode(), ex.getMessage(), ex);
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 全局异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result<?> handleException(Exception e) {
        logger.info("请求：[{}] {} ", request.getMethod(), request.getRequestURI());

        logger.error("出现异常", e);
        return Result.serverError();
    }
}
