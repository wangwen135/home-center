package com.wwh.home.center.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * /checkAuth 限流状态（按 IP）
 */
@ApiModel(description = "限流状态")
@Data
public class RateLimitStatusVo {

    @ApiModelProperty("客户端IP")
    private String ip;

    @ApiModelProperty("当前窗口内请求数")
    private long windowCount;

    @ApiModelProperty("累计请求数")
    private long totalCount;

    @ApiModelProperty("被拦截次数")
    private long rejectedCount;

    @ApiModelProperty("最近请求时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastRequestTime;

    @ApiModelProperty("是否已被封禁")
    private boolean blocked;

    @ApiModelProperty("封禁截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime blockedUntilTime;
}
