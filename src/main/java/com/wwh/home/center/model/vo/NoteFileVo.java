package com.wwh.home.center.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 笔记对象
 *
 * @author wangwh
 * @date 2024/02/22
 */
@ApiModel("笔记对象")
@Data
public class NoteFileVo {

    @ApiModelProperty("父路径")
    private String parentPath;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("文件内容")
    private String content;

    @ApiModelProperty("文件类型")
    private String fileType;

    //其他特性

    @ApiModelProperty("收藏")
    private boolean favorite;

    @ApiModelProperty("标星")
    private boolean asterisk;


    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
