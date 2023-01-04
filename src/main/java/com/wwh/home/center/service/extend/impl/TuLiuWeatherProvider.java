package com.wwh.home.center.service.extend.impl;

import com.alibaba.fastjson.JSONObject;
import com.wwh.home.center.model.vo.WeatherVo;
import com.wwh.home.center.service.extend.WeatherProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 土流中台
 *
 * @author wangwh
 * @date 2023/01/04
 */
@Slf4j
@Service
public class TuLiuWeatherProvider implements WeatherProvider {
    // 纬度
    public static String LAT = "28.152314";
    // 经度
    public static String LON = "113.06269";

    public static String CLIENT_ID = "diduoduo";

    //public static String WEATHER_URL = "http://localhost:51089/common/weather/condition";
    public static String WEATHER_URL = "https://tc-gateway.tuliu.com/common/weather/condition?client_id=diduoduo";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Type getType() {
        return Type.土流;
    }


    @Override
    public WeatherVo getWeather() {
        // 请求头设置,x-www-form-urlencoded格式的数据
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("client_id", CLIENT_ID);
        headers.set("Client-Id", CLIENT_ID);

        //提交参数设置
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("lat", LAT);
        map.add("lon", LON);

        // 组装请求体
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        JSONObject obj = restTemplate.postForObject(WEATHER_URL, request, JSONObject.class);
        log.debug("获取天气信息返回结果：{}", obj);
        if (obj.getInteger("code") != 200) {
            log.warn("获取天气信息返回结果非200：{}", obj);
            return null;
        }
        JSONObject condObj = obj.getJSONObject("data").getJSONObject("condition");
        log.debug("天气：{}", condObj);

        WeatherVo vo = new WeatherVo();
        vo.setText(condObj.getString("condition"));
        vo.setTemp(condObj.getString("temp"));
        vo.setFeelsLike(condObj.getString("realFeel"));
        vo.setHumidity(condObj.getString("humidity"));
        vo.setTips(condObj.getString("tips"));
        vo.setUpdatetime(condObj.getString("updatetime"));
        vo.setIconUrl(condObj.getString("smallIconUrl"));

        return vo;
    }

}
