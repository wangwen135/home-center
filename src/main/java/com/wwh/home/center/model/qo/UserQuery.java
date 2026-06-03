package com.wwh.home.center.model.qo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 用户查询对象
 *
 * @author wangwh
 * @date 2021-12-15
 */
@ApiModel("用户查询对象")
@Data
@ToString
public class UserQuery {

    @ApiModelProperty("性别 1：男 2：女")
    private Integer gender;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("是否锁定")
    private Boolean locked;

    @ApiModelProperty("创建人")
    private Long createBy;
}
