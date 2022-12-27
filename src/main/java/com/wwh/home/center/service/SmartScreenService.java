package com.wwh.home.center.service;

import com.wwh.home.center.model.vo.WeatherVo;

/**
 * 智能屏
 *
 * @author wangwh
 * @date 2022/12/27
 */
public interface SmartScreenService {

    String getRandomFamous();

    WeatherVo getWeather();
}
