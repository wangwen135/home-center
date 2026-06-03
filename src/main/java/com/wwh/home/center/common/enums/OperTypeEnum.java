package com.wwh.home.center.common.enums;

/**
 * 操作类型
 *
 * @author wangwh
 * @date 2024/03/14
 */
public enum OperTypeEnum {
    OTHER("other", "其它"),

    INSERT("insert", "新增"),

    UPDATE("update", "编辑"),

    DELETE("delete", "删除"),

    EXPORT("export", "导出"),

    IMPORT("import", "导入");


    private final String code;
    private final String info;

    OperTypeEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
