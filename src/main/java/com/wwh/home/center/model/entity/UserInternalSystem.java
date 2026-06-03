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
 * 用户与内部系统关联表
 * </p>
 *
 * @author wwh
 * @since 2024-01-24
 */
@Getter
@Setter
@TableName("user_internal_system")
@ApiModel(value = "UserInternalSystem对象", description = "用户与内部系统关联表")
public class UserInternalSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("系统ID")
    private Integer sysId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户备注信息")
    private String remark;

    @ApiModelProperty("0 正常 1 已删除")
    private Boolean deleted;

    @ApiModelProperty("创建人")
    private Integer createBy;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改人")
    private Integer updateBy;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;


}
