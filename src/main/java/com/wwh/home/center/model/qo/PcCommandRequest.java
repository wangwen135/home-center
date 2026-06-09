package com.wwh.home.center.model.qo;

import lombok.Data;

/**
 * PC 远程命令请求
 */
@Data
public class PcCommandRequest {

    private String command;

    private Integer timeoutSeconds;
}
