package com.wwh.home.center.controller.device;

import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import com.wwh.home.center.device.tools.SimpleSocketSender;
import com.wwh.home.center.model.common.ApiResponse;
import com.wwh.home.center.model.entity.PcDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/device/pc")
public class PcMonitorController {

    @Autowired
    private PcDeviceMapper pcDeviceMapper;

    @Autowired
    private SimpleSocketSender simpleSocketSender;

    @Value("${agent.screenshot-dir:/opt/home-center/screenshots}")
    private String screenshotDir;

    @Value("${agent.screenshot-wait-seconds:8}")
    private int screenshotWaitSeconds;

    @PostMapping("/{deviceId}/screenshot")
    public ApiResponse<Map<String, String>> captureScreenshot(@PathVariable Long deviceId) {
        try {
            PcDevice device = pcDeviceMapper.selectById(deviceId);
            if (device == null || !Integer.valueOf(1).equals(device.getStatus())) {
                return ApiResponse.error("设备不存在或已禁用");
            }

            long startMillis = System.currentTimeMillis();
            simpleSocketSender.sendCommand(device, "screenshot");

            Path screenshot = waitLatestScreenshot(deviceId, startMillis);
            if (screenshot == null) {
                return ApiResponse.error("已发送截图指令，但未等到截图上传");
            }

            Map<String, String> data = new HashMap<>();
            data.put("url", "/device/pc/" + deviceId + "/screenshot/latest");
            data.put("path", screenshot.toString());
            return ApiResponse.success("截图已更新", data);
        } catch (Exception e) {
            log.error("触发PC截图失败: deviceId={}", deviceId, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{deviceId}/screenshot/latest")
    public ResponseEntity<Resource> getLatestScreenshot(@PathVariable Long deviceId) {
        try {
            PcDevice device = pcDeviceMapper.selectById(deviceId);
            if (device == null || !Integer.valueOf(1).equals(device.getStatus())) {
                return ResponseEntity.notFound().build();
            }

            Path screenshot = findLatestScreenshot(deviceId);
            if (screenshot == null) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(screenshot);
            MediaType mediaType = contentType == null
                    ? MediaType.IMAGE_PNG
                    : MediaType.parseMediaType(contentType);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(new FileSystemResource(screenshot.toFile()));
        } catch (Exception e) {
            log.error("读取PC最新截图失败: deviceId={}", deviceId, e);
            return ResponseEntity.notFound().build();
        }
    }

    private Path waitLatestScreenshot(Long deviceId, long startMillis) throws IOException, InterruptedException {
        long timeoutMillis = Math.max(screenshotWaitSeconds, 1) * 1000L;
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() <= deadline) {
            Path screenshot = findLatestScreenshot(deviceId);
            if (screenshot != null && Files.getLastModifiedTime(screenshot).toMillis() >= startMillis - 1000L) {
                return screenshot;
            }
            TimeUnit.MILLISECONDS.sleep(500L);
        }
        return null;
    }

    private Path findLatestScreenshot(Long deviceId) throws IOException {
        Path root = Paths.get(screenshotDir);
        if (!Files.exists(root)) {
            return null;
        }

        Path deviceScreenshot = findLatestScreenshot(root, "device-" + deviceId + "-screenshot-");
        if (deviceScreenshot != null) {
            return deviceScreenshot;
        }
        return findLatestScreenshot(root, "screenshot-");
    }

    private Path findLatestScreenshot(Path root, String filenamePrefix) throws IOException {
        try (Stream<Path> stream = Files.walk(root, 3, FileVisitOption.FOLLOW_LINKS)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> isScreenshotFile(path, filenamePrefix))
                    .max(Comparator.comparingLong(this::lastModifiedMillis))
                    .orElse(null);
        }
    }

    private boolean isScreenshotFile(Path path, String filenamePrefix) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.startsWith(filenamePrefix)
                && (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg"));
    }

    private long lastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }
}
