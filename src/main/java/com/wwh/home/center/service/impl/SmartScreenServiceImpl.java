package com.wwh.home.center.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wwh.home.center.dao.mapper.PromptMessageMapper;
import com.wwh.home.center.model.entity.FamousQuotes;
import com.wwh.home.center.model.vo.WeatherVo;
import com.wwh.home.center.service.FamousQuotesService;
import com.wwh.home.center.service.SmartScreenService;
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
 * @author wangwh
 * @date 2022/12/27
 */
@Slf4j
@Service
public class SmartScreenServiceImpl implements SmartScreenService {

    // 纬度
    public static String LAT = "28.152314";
    // 经度
    public static String LON = "113.06269";

    public static String CLIENT_ID = "diduoduo";

    //public static String WEATHER_URL = "http://localhost:51089/common/weather/condition";
    public static String WEATHER_URL = "https://tc-gateway.tuliu.com/common/weather/condition?client_id=diduoduo";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FamousQuotesService famousQuotesService;

    @Autowired
    private PromptMessageMapper promptMessageMapper;

    @Override
    public String getRandomFamous() {
        FamousQuotes fq = famousQuotesService.getRandomFamousByCache();
        if (fq == null) {
            log.warn("没有有效的名言名句");
            return "";
        }
        return fq.getFamous();
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
        return JSON.parseObject(condObj.toJSONString(), WeatherVo.class);
    }
}
