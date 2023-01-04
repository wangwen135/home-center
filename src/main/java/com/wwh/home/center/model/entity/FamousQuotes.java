package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 名言名句
 *
 * @author wangwh
 * @date 2022/12/27
 */
@Data
@TableName("famous_quotes")
public class FamousQuotes {
    @TableId
    private Integer id;
    private String famous;
    private Integer weight;
    private Boolean fixedDisplay;
    private Date createTime;
    private Boolean deleted;
    private String remark;
}
