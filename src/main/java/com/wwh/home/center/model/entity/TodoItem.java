package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户待办事项
 *
 * @author wangwh
 */
@Data
@TableName("todo_item")
public class TodoItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户
     */
    private Integer userId;

    /**
     * 内容
     */
    private String content;

    /**
     * 优先级：high/medium/low
     */
    private String priority;

    /**
     * 是否已完成
     */
    private Boolean completed;

    /**
     * 标签，英文逗号分隔
     */
    private String tags;

    /**
     * 是否已删除（软删除，不物理删除）
     */
    private Boolean deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
