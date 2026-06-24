package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.SysConfig;
import com.wwh.home.center.model.vo.SiteInfoVo;

import java.util.List;

public interface SysConfigService {

    List<SysConfig> listAll();

    SysConfig getByKey(String key);

    void updateConfig(String key, String value);

    /**
     * 公开站点信息（站点名 / Logo / 描述），缺失项以默认值兜底。
     */
    SiteInfoVo getPublicSiteInfo();
}
