package com.wwh.home.center.controller;

import com.wwh.home.center.model.entity.CameraConfig;
import com.wwh.home.center.service.CameraVideoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Value("${camera-video.base-path}")
    private String videoBasePath;

    @GetMapping("/video/path")
    public String getVideoBasePath() {
        return videoBasePath;
    }

    @GetMapping("/list")
    public List<Map<String, String>> list() {
        log.debug("获取摄像头相关配置");

        List<CameraConfig> list = cameraVideoService.getCameraConfig();

        return list.stream().map(cc -> {
            Map<String, String> m = new HashMap<>();
            m.put("name", cc.getName());
            m.put("code", cc.getCode());
            return m;
        }).collect(Collectors.toList());

    }

    /**
     * 获取摄像头有视频的日期
     *
     * @param code
     * @return
     */
    @GetMapping("/video/date")
    public List<String> getVideoDate(@RequestParam String code) {
        File baseDir = new File(videoBasePath);
        File videoDir = new File(baseDir, code);
        String[] dayHours = videoDir.list();
        return Arrays.stream(dayHours).map(ymmddhh -> ymmddhh.substring(0, 8)).distinct().collect(Collectors.toList());
    }

    /**
     * 获取某个摄像头某个日期下面有视频的小时
     *
     * @param code
     * @param date
     * @return
     */
    @GetMapping("/video/hour")
    public List<String> getVideoHour(@RequestParam String code, @RequestParam String date) {
        File baseDir = new File(videoBasePath);
        File videoDir = new File(baseDir, code);
        String[] dayHours = videoDir.list((dir, name) -> name.startsWith(date));
        return Arrays.stream(dayHours).map(x -> x.substring(8)).collect(Collectors.toList());
    }

    /**
     * 获取某个摄像头某个日期下面某小时有视频的分钟
     *
     * @param code
     * @param date
     * @param hour
     * @return
     */
    @GetMapping("/video/minute")
    public List<String> getVideoMinute(@RequestParam String code, @RequestParam String date, @RequestParam String hour) {
        File baseDir = new File(videoBasePath);
        File videoDir = new File(baseDir, code);
        File ymdhDir = new File(videoDir, date + hour);
        String[] minutes = ymdhDir.list();
        return Arrays.stream(minutes).map(x -> x.substring(0, 2)).collect(Collectors.toList());
    }

    @GetMapping("/getVideo")
    public void getVideoStream(@RequestParam String code, @RequestParam String date, @RequestParam String hour, @RequestParam String minute, HttpServletResponse response) {

        try {
            File baseDir = new File(videoBasePath);
            File videoDir = new File(baseDir, code);
            File ymdhDir = new File(videoDir, date + hour);
            File[] videoFile = ymdhDir.listFiles((dir, name) -> name.startsWith(minute));
            if (videoFile == null || videoFile.length == 0) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.getOutputStream().write("video file not found".getBytes());
                log.warn("视频文件没有找到，路径：{}，分钟：{}", ymdhDir.getAbsolutePath(), minute);
                return;
            }
            //每分钟只有一个文件
            File file = videoFile[0];
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            InputStream iStream = new FileInputStream(file);
            IOUtils.copy(iStream, response.getOutputStream());
            response.flushBuffer();
        } catch (NoSuchFileException e) {
            log.error("视频文件没找到", e);
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } catch (Exception e) {
            log.error("视频流处理异常", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
