package com.wwh.home.center.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 天气
 *
 * @author wangwh
 * @date 2023/01/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherVo {

    /**
     * 天气状况的文字描述
     */
    private String text;// ": "晴",
    /**
     * 温度（摄氏度）
     */
    private String temp;//  "24",
    /**
     * 体感温度（摄氏度）
     */
    private String feelsLike;// "18",
    /**
     * 相对湿度，百分比数值
     */
    private String humidity;// "42",
    /**
     * 天气提示
     */
    private String tips;//  "冷热适宜，感觉很舒适。",
    //
    /**
     * 更新时间
     */
    private String updatetime;//  "2016-09-01 22:03:00",
    /**
     * 天气图标地址
     */
    private String iconUrl;
}
