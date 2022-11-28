package com.wwh.home.center.controller;

import com.wwh.home.center.service.CameraVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 摄像头视频相关
 *
 * @author wangwh
 * @date 2022/11/28
 */
@Slf4j
@RestController
@RequestMapping("/camera")
public class CameraVideoController {

    @Autowired
    private CameraVideoService cameraVideoService;

    @RequestMapping("/list")
    public Object list() {
        log.debug("获取摄像头相关配置");
        return cameraVideoService.getCameraConfig();
    }

}
