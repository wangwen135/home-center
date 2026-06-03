package com.wwh.home.center.common.util;

import com.wwh.home.center.common.model.PageInfo;
import com.wwh.home.center.model.qo.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 分页助手
 *
 * @author wangwh
 * @date 2023/05/04
 */
public class PageHelper {

    /**
     * 转换成分页对象
     *
     * @param <T>
     * @return
     */
    public static <T> PageInfo<T> pageQuery2PageInfo(PageQuery pq) {
        return PageInfo.of(pq.getPageNum(), pq.getPageSize());
    }

    /**
     * 转换成MybatisPlus的Page对象
     *
     * @return
     */
    public static Page pageInfo2MybatisPlusPage(PageInfo pi) {
        return new Page(pi.getPageNum(), pi.getPageSize());
    }


}
