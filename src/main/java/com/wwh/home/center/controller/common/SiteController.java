package com.wwh.home.center.controller.common;

import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.vo.SiteInfoVo;
import com.wwh.home.center.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开站点信息接口（免登录）。
 * <p>仅暴露站点名 / Logo / 描述等公开字段，不泄露任何敏感配置。</p>
 *
 * @author wwh
 */
@Slf4j
@Api(tags = "公开站点信息接口")
@RestController
@RequestMapping("/api/site")
public class SiteController {

    @Autowired
    private SysConfigService sysConfigService;

    @ApiOperation("站点公开信息（站点名 / Logo / 描述）")
    @GetMapping("/info")
    public Result<SiteInfoVo> info() {
        return Result.success(sysConfigService.getPublicSiteInfo());
    }
}
