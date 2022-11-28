package com.wwh.home.center.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;

/**
 * TODO
 *
 * @author WWH
 * @version 1.0
 * @date 2022/11/28 21:35
 */
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
    // @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
