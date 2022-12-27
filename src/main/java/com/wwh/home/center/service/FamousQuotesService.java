package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.FamousQuotes;

import java.util.List;

/**
 * @author wangwh
 * @date 2022/12/27
 */
public interface FamousQuotesService {

    /**
     * 获取一条随机的
     *
     * @return
     */
    FamousQuotes getRandomFamous();

    /**
     * 获取全部
     *
     * @return
     */
    List<FamousQuotes> getAllFamous();

    /**
     * 获取一条随机的，通过缓存，30分钟内不会改变
     *
     * @return
     */
    FamousQuotes getRandomFamousByCache();
}
