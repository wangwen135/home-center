package com.wwh.home.center.common.annotation;

import com.wwh.home.center.common.enums.OperTypeEnum;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 *
 * @author wangwh
 * @date 2024/03/14
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
    
    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作类型
     */
    OperTypeEnum operType() default OperTypeEnum.OTHER;
}
