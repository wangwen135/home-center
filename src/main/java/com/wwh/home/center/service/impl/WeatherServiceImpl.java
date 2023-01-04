package com.wwh.home.center.service.impl;

import com.wwh.home.center.model.vo.WeatherVo;
import com.wwh.home.center.service.WeatherService;
import com.wwh.home.center.service.extend.WeatherProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 天气服务
 *
 * @author wangwh
 * @date 2023/01/03
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    @Value("${weather.provider}")
    private String providerName;

    @Autowired
    private List<WeatherProvider> weatherProviders;

    private WeatherProvider realWeatherProvider;

    private WeatherProvider getRealWeatherProvider() {
        if (realWeatherProvider != null) {
            return realWeatherProvider;
        }
        for (WeatherProvider wp : weatherProviders) {
            if (wp.getType().toString().equals(providerName)) {
                realWeatherProvider = wp;
                break;
            }
        }
        return realWeatherProvider;
    }

    @Override
    public WeatherVo getWeather() {
        return getRealWeatherProvider().getWeather();
    }
}
