package com.wwh.home.center.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 天气
 *
 * @author wangwh
 * @date 2023/01/04
 */
@ApiModel(description = "天气信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherVo {

    @ApiModelProperty("天气状况的文字描述")
    private String text;// ": "晴",

    @ApiModelProperty("温度（摄氏度）")
    private String temp;//  "24",

    @ApiModelProperty("体感温度（摄氏度）")
    private String feelsLike;// "18",

    @ApiModelProperty("相对湿度，百分比数值")
    private String humidity;// "42",

    @ApiModelProperty("天气提示")
    private String tips;//  "冷热适宜，感觉很舒适。",

    @ApiModelProperty("更新时间")
    private String updatetime;//  "2016-09-01 22:03:00",

    @ApiModelProperty("天气图标地址")
    private String iconUrl;
}
