package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwh.home.center.dao.mapper.CameraConfigMapper;
import com.wwh.home.center.model.entity.CameraConfig;
import com.wwh.home.center.service.CameraVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TODO
 *
 * @author WWH
 * @version 1.0
 * @date 2022/11/28 21:13
 */
@Service
public class CameraVideoServiceImpl implements CameraVideoService {

    @Autowired
    private CameraConfigMapper cameraConfigMapper;

    @Override
    public List<CameraConfig> getCameraConfig() {
        QueryWrapper<CameraConfig> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("deleted", false);
        queryWrapper.lambda().eq(CameraConfig::getDeleted, false);
        return cameraConfigMapper.selectList(queryWrapper);
    }
}
