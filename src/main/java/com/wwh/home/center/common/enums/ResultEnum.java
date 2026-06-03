package com.wwh.home.center.common.enums;

/**
 * 结果枚举
 *
 * @author wangwh
 * @date 2021-3-15
 */
public enum ResultEnum {

    /**
     * 0 未处理的
     */
    UNTREATED(0),
    /**
     * 1 处理成功
     */
    SUCCESS(1),
    /**
     * 2 处理异常的
     */
    EXCEPTION(2),
    /**
     * 3 不再处理，废弃的
     */
    DEAD(3),
    /**
     * 9 可补偿，可修复的
     */
    REPAIRABLE(9);

    private int code;

    private ResultEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
