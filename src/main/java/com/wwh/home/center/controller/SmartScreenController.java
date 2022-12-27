package com.wwh.home.center.controller;

import com.wwh.home.center.model.vo.WeatherVo;
import com.wwh.home.center.service.SmartScreenService;
import com.wwh.home.center.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能屏
 *
 * @author wangwh
 * @date 2022/12/27
 */
@Slf4j
@RestController
@RequestMapping("/smartScreen")
public class SmartScreenController {

    @Autowired
    private SmartScreenService smartScreenService;

    @GetMapping("/test")
    public String test() {
        return "this is test msg";
    }

    @GetMapping("/getRandomFamous")
    public String getRandomFamous() {
        return smartScreenService.getRandomFamous();
    }

    @GetMapping("/getNongLi")
    public String getNongLi() {
        return DateUtils.getNongLi();
    }

    @GetMapping("/getNongLiShort")
    public String getNongLiShort() {
        return DateUtils.getNongLiShort();
    }

    @GetMapping("/getDateAndWeek")
    public String getDateAndWeek() {
        return DateUtils.getDateAndWeek();
    }

    @GetMapping("/getWeather")
    public WeatherVo getWeather() {
        try {
            return smartScreenService.getWeather();
        } catch (Exception e) {
            log.error("获取天气信息异常", e);
            return null;
        }
    }
}
