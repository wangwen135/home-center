package com.wwh.home.center.service.extend.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wwh.home.center.model.vo.WeatherVo;
import com.wwh.home.center.service.extend.WeatherProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * 和风
 *
 * @author wangwh
 * @date 2023/01/04
 */
@Slf4j
@Service
public class HeFengWeatherProvider implements WeatherProvider {

    @Value("${weather.config.hefeng.now-url}")
    private String nowUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Type getType() {
        return Type.和风;
    }

    @Override
    public WeatherVo getWeather() {
        HttpHeaders httpHeaders = new HttpHeaders();

        // Accept-Encoding 头，表示客户端接收gzip格式的压缩
        httpHeaders.set(HttpHeaders.ACCEPT_ENCODING, "gzip");

        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(nowUrl, HttpMethod.GET, new HttpEntity<>(httpHeaders), byte[].class);

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.warn("和风实时天气响应状态异常：{}", responseEntity.getStatusCode());
            return null;
        }
        // gzip解压服务器的响应体
        byte[] data = new byte[0];
        try {
            data = unGZip(new ByteArrayInputStream(responseEntity.getBody()));
        } catch (IOException e) {
            log.error("unGZip异常", e);
            throw new RuntimeException("unGZip 异常");
        }

        String ret = new String(data, StandardCharsets.UTF_8);
        log.debug("和风实时天气返回结果：{}", ret);
        JSONObject retJson = JSON.parseObject(ret);

        String code = retJson.getString("code");
        if (!"200".equals(code)) {
            log.error("和风实时天气返回结果异常：{}", ret);
            return null;
        }
        WeatherVo vo = new WeatherVo();
        JSONObject now = retJson.getJSONObject("now");
        vo.setText(now.getString("text"));
        vo.setTemp(now.getString("temp"));
        vo.setFeelsLike(now.getString("feelsLike"));
        vo.setHumidity(now.getString("humidity"));
        vo.setUpdatetime(now.getString("obsTime"));
        String icon = now.getString("icon");
        vo.setIconUrl("/weather/hefeng/icons/" + icon + ".svg");
        return vo;
    }

    /**
     * Gzip解压缩
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] unGZip(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
            byte[] buf = new byte[4096];
            int len = -1;
            while ((len = gzipInputStream.read(buf, 0, buf.length)) != -1) {
                byteArrayOutputStream.write(buf, 0, len);
            }
            return byteArrayOutputStream.toByteArray();
        } finally {
            byteArrayOutputStream.close();
        }
    }

}
