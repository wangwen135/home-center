package com.wwh.home.center.service.extend;

import com.wwh.home.center.model.vo.WeatherVo;

/**
 * 具体天气提供者
 *
 * @author wangwh
 * @date 2023/01/04
 */
public interface WeatherProvider {

    enum Type {
        土流, 和风
    }

    /**
     * 获取提供者类型
     *
     * @return
     */
    Type getType();

    /**
     * 获取天气信息
     *
     * @return
     */
    WeatherVo getWeather();

}
