package com.wwh.home.center.controller;

import com.wwh.home.center.model.vo.WeatherVo;
import com.wwh.home.center.service.WeatherService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气接口
 *
 * @author wangwh
 * @date 2023/01/03
 */
@Api(tags = "天气接口")
@Slf4j
@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/getWeather")
    public WeatherVo getWeather() {
        try {
            return weatherService.getWeather();
        } catch (Exception e) {
            log.error("获取天气信息异常", e);
            return null;
        }
    }
}
