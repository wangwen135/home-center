package com.wwh.home.center.model.qo;

import com.wwh.home.center.common.model.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;

/**
 * <pre>
 *  PageQuery 对象可以包含分页查询所需的各种参数，例如页码、每页条数、排序方式、查询条件等
 * </pre>
 *
 * @author wangwh
 * @date 2023/04/28
 */
@ApiModel("分页信息")
@AllArgsConstructor
public class PageQuery {
    @ApiModelProperty("页码, 默认1")
    private Integer pageNum = 1;

    @ApiModelProperty("条数, 默认20")
    private Integer pageSize = 20;


    public Integer getPageNum() {
        return pageNum == null ? 1 : pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize == null ? 20 : pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 转换成分页对象
     *
     * @param <T>
     * @return
     */
    public <T> PageInfo<T> convert() {
        return PageInfo.of(getPageNum(), getPageSize());
    }
}