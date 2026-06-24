package com.wwh.home.center.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 站点公开信息（站点名 / Logo / 描述）。
 * <p>用于公开首页、登录页等免登录页面展示站点品牌；缺失项由后端填充默认值。</p>
 *
 * @author wwh
 */
@Data
@ApiModel("站点公开信息")
public class SiteInfoVo {

    @ApiModelProperty("站点名称")
    private String name;

    @ApiModelProperty("站点 Logo（图片 URL 或 emoji/文字；空则前端用默认图标）")
    private String logo;

    @ApiModelProperty("站点描述（标题下方一行小字）")
    private String tagline;
}
