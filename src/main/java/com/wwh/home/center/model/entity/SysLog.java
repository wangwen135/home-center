package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 系统日志表
 * </p>
 *
 * @author wwh
 * @since 2024-01-08
 */
@Data
@TableName("sys_log")
@ApiModel(value = "SysLog对象", description = "系统日志表")
public class SysLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("系统时间")
    private LocalDateTime sysTime;

    @ApiModelProperty("操作人ID")
    private Integer operatorId;

    @ApiModelProperty("操作人名称")
    private String operatorName;

    @ApiModelProperty("日志类型")
    private String logType;

    @ApiModelProperty("操作内容")
    private String content;

    @ApiModelProperty("ip地址")
    private String ip;

    @ApiModelProperty("浏览器信息")
    private String browserInfo;


}
