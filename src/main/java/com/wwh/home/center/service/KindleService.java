package com.wwh.home.center.service;

import com.wwh.home.center.model.vo.WeatherVo;

/**
 * Kindle信息展示
 *
 * @author wangwh
 * @date 2022/12/27
 */
public interface KindleService {

    String getRandomFamous();

    WeatherVo getWeather();
}
