package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.dao.mapper.SysConfigMapper;
import com.wwh.home.center.model.entity.SysConfig;
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
        if (config == null) {
            throw new BusinessException("配置不存在：" + key);
        }
        SysConfig update = new SysConfig();
        update.setId(config.getId());
        update.setConfigValue(value);
        update.setUpdateTime(LocalDateTime.now());
        sysConfigMapper.updateById(update);
        log.info("更新系统配置成功，key={}", key);
    }
}
