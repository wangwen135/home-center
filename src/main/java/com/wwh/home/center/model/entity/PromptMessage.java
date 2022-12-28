package com.wwh.home.center.model.entity;

/**
 * 提示消息
 *
 * @author wangwh
 * @date 2022/12/27
 */

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("prompt_message")
public class PromptMessage {
    @TableId
    private Integer id;
    private String message;
    private Date createTime;
    private Date expirationTime;
    private Integer weight;
    private Boolean showOnly;
    private Boolean deleted;
    private String remark;
}
