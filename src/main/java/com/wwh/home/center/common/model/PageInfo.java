package com.wwh.home.center.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 分页对象，返回数据用
 *
 * @param <T>
 * @author wangwh
 * @date 2021-4-9
 */
@ApiModel("分页对象")
public class PageInfo<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认页
     */
    public static final long DEFAULT_PAGE_NUM = 1;

    /**
     * 默认页大小
     */
    public static final long DEFAULT_PAGE_SIZE = 20;

    /**
     * 页码
     */
    @ApiModelProperty("页码")
    private long pageNum;

    /**
     * 页大小
     */
    @ApiModelProperty("页大小")
    private long pageSize;

    /**
     * 总记录数
     */
    @ApiModelProperty("总记录数")
    private long total;

    /**
     * 结果集
     */
    @ApiModelProperty("结果集")
    private List<T> data;

    public PageInfo() {
        this.pageNum = DEFAULT_PAGE_NUM;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    public PageInfo(int pageNum, int pageSize) {
        this.pageNum = pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
        this.pageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
    }

    public static <T> PageInfo<T> of(int pageNum, int pageSize) {
        return new PageInfo<T>(pageNum, pageSize);
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    /**
     * 查询结果的size
     *
     * @return
     */
    @ApiModelProperty("结果集大小")
    public int getDataSize() {
        return data == null ? 0 : data.size();
    }

    /**
     * 总页数
     *
     * @return
     */
    @ApiModelProperty("总页数")
    public long getPages() {
        if (total <= 0) {
            return 0;
        }
        return (total + pageSize - 1) / pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPageNum() {
        return pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }


}
