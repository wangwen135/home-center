package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 操作日志
 * </p>
 *
 * @author wangwh
 * @since 2024-03-14
 */
@Getter
@Setter
@TableName("operation_log")
@ApiModel(value = "OperationLog对象", description = "操作日志")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "oper_id", type = IdType.AUTO)
    private Long operId;

    @ApiModelProperty("用户id")
    private Integer userId;

    @ApiModelProperty("操作时间")
    private LocalDateTime operTime;

    @ApiModelProperty("操作类型")
    private String operType;

    @ApiModelProperty("功能模块")
    private String module;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("请求参数")
    private String operParam;

    @ApiModelProperty("请求方法")
    private String method;

    @ApiModelProperty("异常信息")
    private String errorMsg;

    @ApiModelProperty("状态 0正常 1异常")
    private Integer status;

    @ApiModelProperty("请求url")
    private String operUrl;

    @ApiModelProperty("接口返回json")
    private String jsonResult;


}
