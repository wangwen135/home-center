package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * TODO
 *
 * @author WWH
 * @version 1.0
 * @date 2022/11/28 21:09
 */
@Data
@TableName("camera_config")
public class CameraConfig {
    @TableId
    private Integer id;
    private String name;
    private String code;
    private Date createTime;
    private Boolean deleted;
    private String remark;
}
