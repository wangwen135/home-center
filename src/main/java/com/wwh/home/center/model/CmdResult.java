package com.wwh.home.center.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * PC Agent 远程命令执行结果
 * 字段与客户端 CmdResult.toJson() 输出对齐
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmdResult {

    /** 执行命令（服务端填充） */
    private String command;

    /** 进程退出码 */
    private Integer exitCode;

    /** 标准输出 */
    private String stdout;

    /** 错误输出 */
    private String stderr;

    /** 是否成功 */
    private Boolean success;

    /** 是否超时（客户端字段名 timedOut） */
    private Boolean timedOut;

    /** 输出是否被截断 */
    private Boolean truncated;

    /** 执行耗时(ms) */
    private Long durationMillis;

    /** 错误信息（客户端字段名 error） */
    private String error;
}
