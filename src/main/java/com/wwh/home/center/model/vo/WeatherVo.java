package com.wwh.home.center.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 天气
 *
 * @author wangwh
 * @date 2022/12/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherVo {
    //实况天气现象
    private String condition;// ": "晴",
    //温度（摄氏度）
    private String temp;//  "24",
    //体感温度（摄氏度）
    private String realFeel;// "18",
    //湿度
    private String humidity;// "42",
    //天气提示
    private String tips;//  "冷热适宜，感觉很舒适。",
    //天气发布时间
    private String updatetime;//  "2016-09-01 22:03:00",
    //小图标地址
    private String smallIconUrl;
    //大图标地址
    private String bigIconUrl;

}
