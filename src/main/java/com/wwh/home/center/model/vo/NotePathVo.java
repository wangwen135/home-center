package com.wwh.home.center.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.File;

/**
 * 笔记路径对象
 *
 * @author wangwh
 * @date 2024/02/22
 */
@ApiModel("笔记路径对象")
@Data
public class NotePathVo {
    @ApiModelProperty("父路径")
    private String parentPath;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("是否为目录")
    private boolean isDir;

    @ApiModelProperty("文件类型")
    private String fileType;

    @ApiModelProperty("全路径")
    public String getFullPath() {
        //return parentPath + File.separator + name;
        return parentPath + "/" + name;
    }
}
