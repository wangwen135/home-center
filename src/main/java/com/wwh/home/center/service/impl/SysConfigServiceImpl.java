package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.dao.mapper.SysConfigMapper;
import com.wwh.home.center.model.entity.SysConfig;
import com.wwh.home.center.model.vo.SiteInfoVo;
import com.wwh.home.center.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Override
    public List<SysConfig> listAll() {
        return sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getConfigKey));
    }

    @Override
    public SysConfig getByKey(String key) {
        return sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("limit 1"));
    }

    @Override
    public void updateConfig(String key, String value) {
        SysConfig config = getByKey(key);
        LocalDateTime now = LocalDateTime.now();
        if (config == null) {
            // upsert：后台首次维护某配置项时自动创建（如站点名/Logo/描述）
            SysConfig insert = new SysConfig();
            insert.setConfigKey(key);
            insert.setConfigValue(value);
            insert.setCreateTime(now);
            insert.setUpdateTime(now);
            sysConfigMapper.insert(insert);
            log.info("新增系统配置，key={}", key);
            return;
        }
        SysConfig update = new SysConfig();
        update.setId(config.getId());
        update.setConfigValue(value);
        update.setUpdateTime(now);
        sysConfigMapper.updateById(update);
        log.info("更新系统配置成功，key={}", key);
    }

    @Override
    public SiteInfoVo getPublicSiteInfo() {
        SiteInfoVo vo = new SiteInfoVo();
        vo.setName(resolveValue("site.name", "Home Center"));
        vo.setLogo(resolveValue("site.logo", ""));
        vo.setTagline(resolveValue("site.tagline", "个人服务入口"));
        return vo;
    }

    private String resolveValue(String key, String defaultValue) {
        SysConfig config = getByKey(key);
        String value = config == null ? null : config.getConfigValue();
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }
}
