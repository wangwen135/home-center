package com.wwh.home.center.common.aspect;

import com.alibaba.fastjson2.JSON;
import com.wwh.home.center.common.annotation.OperLog;
import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.model.entity.OperationLog;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.OperationLogService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * 操作日志记录处理
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 处理完请求后执行
     *
     * @param joinPoint
     * @param controllerLog
     * @param jsonResult
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, OperLog controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, OperLog controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, OperLog controllerLog, final Exception e, Object jsonResult) {
        try {
            // *========数据库日志=========*//
            OperationLog operationLog = new OperationLog();
            operationLog.setStatus(0);
            operationLog.setOperTime(LocalDateTime.now());
            // 请求的地址
            operationLog.setOperUrl(StringUtils.substring(RequestUtil.getRequestURI(), 0, 255));

            operationLog.setUserId(UserContextHolder.getUserId());
            if (e != null) {
                operationLog.setStatus(1);
                operationLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operationLog.setMethod(className + "." + methodName + "()");
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operationLog, jsonResult);
            // 保存数据库
            operationLogService.saveOperationLog(operationLog);
        } catch (Exception exp) {
            log.error("日志记录切面异常", exp);
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log          日志注解
     * @param operationLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, OperLog log, OperationLog operationLog, Object jsonResult) throws Exception {
        //获取api注解的值
        Class<?> targetCls = joinPoint.getTarget().getClass();
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = targetCls.getDeclaredMethod(ms.getName(), ms.getParameterTypes());
        ApiOperation apiOperation = targetMethod.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            operationLog.setRemark(apiOperation.value());
        }
        // 设置action动作
        operationLog.setOperType(log.operType().getCode());
        // 设置标题
        operationLog.setModule(log.module());
        // 获取参数的信息，传入到数据库中。
        setRequestValue(joinPoint, operationLog);

        operationLog.setJsonResult(StringUtils.substring(JSON.toJSONString(jsonResult), 0, 2000));
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operationLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, OperationLog operationLog) throws Exception {
        HttpServletRequest request = RequestUtil.getRequestFromContextHolder();
        Map<String, String> paramsMap = RequestUtil.getParamMap(request);

        if (paramsMap.isEmpty()) {
            String params = argsArrayToString(joinPoint.getArgs());
            operationLog.setOperParam(StringUtils.substring(params, 0, 2000));
        } else {
            operationLog.setOperParam(StringUtils.substring(JSON.toJSONString(paramsMap), 0, 2000));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                try {
                    if (o != null && !isFilterObject(o)) {
                        String str = JSON.toJSONString(o);
                        params.append(str).append(" ");
                    }
                } catch (Exception e) {
                    log.warn("操作日志参数序列化失败: {}", o.getClass().getName(), e);
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    private boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
