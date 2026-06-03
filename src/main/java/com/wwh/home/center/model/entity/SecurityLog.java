package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 安全日志表
 *
 * @author wangwh
 * @date 2024/09/30
 */
@Data
@TableName("security_log")
@ApiModel(value = "SecurityLog对象", description = "安全日志表")
public class SecurityLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("IP地址")
    private String ipAddress;

    @ApiModelProperty("操作类型")
    private String operationType;

    @ApiModelProperty("操作结果")
    private String operationResult;

    @ApiModelProperty("操作时间")
    private LocalDateTime operationTime;

    @ApiModelProperty("详细描述信息")
    private String description;

    @ApiModelProperty("HTTP 请求方法")
    private String httpMethod;

    @ApiModelProperty("请求地址")
    private String requestUrl;

    @ApiModelProperty("客户端信息")
    private String userAgent;

    @ApiModelProperty("来源 URL")
    private String referrerUrl;

    @ApiModelProperty("地理位置")
    private String location;

    @ApiModelProperty("备注")
    private String remark;
}
