package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long userId;

    private String username;

    private String password;

    private String nickname;

    private Integer gender;

    private String phone;

    private Boolean locked;

    private Boolean deleted;

    private Long createBy;

    private Date createTime;

    private Long updateBy;

    private Date updateTime;

}