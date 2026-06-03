package com.wwh.home.center.model.qo;

import com.wwh.home.center.common.model.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <pre>
 *  PageQuery 对象可以包含分页查询所需的各种参数，例如页码、每页条数、排序方式、查询条件等
 * </pre>
 *
 * @author wangwh
 * @date 2023/04/28
 */
@ApiModel("分页查询")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PageQuery<T> {
    @ApiModelProperty(value = "页码, 默认1", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "条数, 默认20", example = "20")
    private Integer pageSize = 20;

    @ApiModelProperty("查询条件")
    private T condition;

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

    public T getCondition() {
        return condition;
    }

    public void setCondition(T condition) {
        this.condition = condition;
    }
}