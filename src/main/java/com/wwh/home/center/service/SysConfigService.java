package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.SysConfig;

import java.util.List;

public interface SysConfigService {

    List<SysConfig> listAll();

    SysConfig getByKey(String key);

    void updateConfig(String key, String value);
}
