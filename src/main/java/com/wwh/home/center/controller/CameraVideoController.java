package com.wwh.home.center.controller;

import com.wwh.home.center.model.entity.CameraConfig;
import com.wwh.home.center.service.CameraVideoService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.time.Duration;
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
@Api(tags = "摄像头视频")
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
        return Arrays.stream(dayHours).map(ymmddhh -> ymmddhh.substring(0, 8)).sorted().distinct().collect(Collectors.toList());
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
        return Arrays.stream(dayHours).map(x -> x.substring(8)).sorted().collect(Collectors.toList());
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
        return Arrays.stream(minutes).map(x -> x.substring(0, 2)).sorted().collect(Collectors.toList());
    }

    /**
     * 获取视频
     *
     * @param code
     * @param date
     * @param hour
     * @param minute
     * @return
     */
    @GetMapping(value = "/video2/{code}/{date}-{hour}-{minute}.mp4", produces = "video/mp4")
    public ResponseEntity video2(@PathVariable String code, @PathVariable String date, @PathVariable String hour, @PathVariable String minute) {
        System.out.println("请求进来了");

        //每分钟只有一个文件
        File file = new File("D:\\temp\\video\\test.mp4");

        //这个写法是支持断点续传的
        //Accept-Ranges: bytes
        //Content-Range: bytes 6258688-6792415/6792416

        System.out.println(file.canRead());
        System.out.println(file.lastModified());
        System.out.println(file.length());

        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.maxAge(Duration.ofMinutes(5))).eTag("112233445566")
                .body(new FileSystemResource(file));
    }

    /**
     * 获取视频
     *
     * @param code
     * @param date
     * @param hour
     * @param minute
     * @return
     */
    @RequestMapping(value = "/video/{code}/{date}-{hour}-{minute}.mp4") //produces = "video/mp4"
    public ResponseEntity video(WebRequest webRequest, @PathVariable String code, @PathVariable String date, @PathVariable String hour, @PathVariable String minute,
                                @RequestParam(required = false) String op) {
        //视频是不会改变的

        System.out.println("If-Unmodified-Since = " + webRequest.getHeader("If-Unmodified-Since"));
        String rEtag = webRequest.getHeader("If-None-Match");
        System.out.println("If-None-Match = " + rEtag);
        String etag = date + hour + minute;

        if (rEtag != null) {
            rEtag = rEtag.replace("\"", "");
            if (etag.equals(rEtag)) {
                System.out.println("直接返回304");
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
        }

        File baseDir = new File(videoBasePath);
        File videoDir = new File(baseDir, code);
        File ymdhDir = new File(videoDir, date + hour);
        File[] videoFile = ymdhDir.listFiles((dir, name) -> name.startsWith(minute));
        if (videoFile == null || videoFile.length == 0) {
            log.warn("视频文件没有找到，路径：{}，分钟：{}", ymdhDir.getAbsolutePath(), minute);
            return new ResponseEntity("video file not found", HttpStatus.NOT_FOUND);
        }
        //每分钟只有一个文件
        File file = videoFile[0];
        String fileName = date + "-" + hour + "-" + minute + ".mp4";
        //这个写法是支持断点续传的
        //Accept-Ranges: bytes
        //Content-Range: bytes 6258688-6792415/6792416
        if ("download".equals(op)) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .body(new FileSystemResource(file));
        } else {
            return ResponseEntity.ok().contentType(MediaType.valueOf("video/mp4"))
                    .cacheControl(org.springframework.http.CacheControl.maxAge(Duration.ofMinutes(5)).noTransform().mustRevalidate().cachePrivate()).eTag(etag)
                    .lastModified(file.lastModified())
                    .body(new FileSystemResource(file));
        }

    }

    //这个写法被淘汰了
    public void getVideoStream2(@RequestParam String code, @RequestParam String date, @RequestParam String hour, @RequestParam String minute, HttpServletResponse response) {

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
            response.setContentLength((int) file.length());
            //response.setHeader("Accept-Ranges", "bytes"); 不支持
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
