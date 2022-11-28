package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.CameraConfig;

import java.util.List;

/**
 * 摄像头视频相关
 *
 * @author wangwh
 * @date 2022/11/28
 */
public interface CameraVideoService {
    List<CameraConfig> getCameraConfig();
}
