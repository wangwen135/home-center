package com.wwh.home.center.common.enums;

public enum OperationRiskLevelEnum {

    NORMAL("normal", "普通操作"),
    SENSITIVE("sensitive", "敏感操作"),
    DANGEROUS("dangerous", "危险操作");

    private final String code;
    private final String description;

    OperationRiskLevelEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
