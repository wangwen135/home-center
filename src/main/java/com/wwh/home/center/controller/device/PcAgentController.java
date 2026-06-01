package com.wwh.home.center.controller.device;

import com.wwh.home.center.common.model.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PC Agent 接口
 * 提供截图上传、版本查询、最新版本下载功能
 *
 * @author wangwh
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Api(tags = "PC Agent 接口")
public class PcAgentController {

    private static final String CURRENT_VERSION = "1.0";

    @Value("${agent.screenshot-dir:/opt/home-center/screenshots}")
    private String screenshotDir;

    @Value("${agent.release-dir:/opt/home-center/releases}")
    private String releaseDir;

    /**
     * 接收 pc-agent 上传的截图
     */
    @PostMapping("/screenshot")
    @ApiOperation("上传截图")
    public Result<Map<String, String>> uploadScreenshot(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.badRequest("上传文件为空");
        }

        try {
            // 按日期创建子目录
            String dateDir = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Path dirPath = Paths.get(screenshotDir, dateDir);
            Files.createDirectories(dirPath);

            // 生成文件名：screenshot-yyyyMMdd-HHmmss.png
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String originalFilename = file.getOriginalFilename();
            String ext = "png";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }
            String filename = "screenshot-" + timestamp + "." + ext;

            Path filePath = dirPath.resolve(filename);
            file.transferTo(filePath.toFile());

            log.info("截图已保存: {}", filePath);

            Map<String, String> data = new HashMap<>();
            data.put("filename", filename);
            data.put("path", filePath.toString());
            data.put("message", "截图已保存");

            return Result.success(data);
        } catch (IOException e) {
            log.error("保存截图失败", e);
            return Result.error(500, "截图保存失败: " + e.getMessage());
        }
    }

    /**
     * 返回最新版本信息
     */
    @GetMapping("/version")
    @ApiOperation("获取最新版本信息")
    public Result<Map<String, String>> getLatestVersion() {
        Map<String, String> data = new HashMap<>();

        try {
            File releasePath = new File(releaseDir);
            if (!releasePath.exists()) {
                releasePath.mkdirs();
            }

            File[] jarFiles = releasePath.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles == null || jarFiles.length == 0) {
                data.put("version", CURRENT_VERSION);
                data.put("downloadUrl", "");
                data.put("releaseTime", "");
                return Result.success(data);
            }

            // 按修改时间排序，找最新的 jar
            File latestJar = Collections.max(Arrays.asList(jarFiles),
                    Comparator.comparingLong(File::lastModified));

            String version = extractVersion(latestJar.getName());
            String releaseTime = LocalDateTime
                    .ofInstant(new Date(latestJar.lastModified()).toInstant(), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            data.put("version", version);
            data.put("downloadUrl", "/api/download/latest");
            data.put("releaseTime", releaseTime);

            log.info("最新版本: {}, 文件: {}", version, latestJar.getName());

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取版本信息失败", e);
            data.put("version", CURRENT_VERSION);
            data.put("downloadUrl", "");
            data.put("releaseTime", "");
            return Result.success(data);
        }
    }

    /**
     * 下载最新的 jar 文件
     */
    @GetMapping("/download/latest")
    @ApiOperation("下载最新版本")
    public ResponseEntity<Resource> downloadLatest() {
        try {
            File releasePath = new File(releaseDir);
            if (!releasePath.exists()) {
                return ResponseEntity.notFound().build();
            }

            File[] jarFiles = releasePath.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles == null || jarFiles.length == 0) {
                return ResponseEntity.notFound().build();
            }

            // 按修改时间排序，找最新的 jar
            File latestJar = Collections.max(Arrays.asList(jarFiles),
                    Comparator.comparingLong(File::lastModified));

            String version = extractVersion(latestJar.getName());
            String downloadFilename = "home-center-pc-agent-" + version + ".jar";

            Resource resource = new FileSystemResource(latestJar);

            log.info("下载最新版本: {}, 文件: {}", version, downloadFilename);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + downloadFilename + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 从文件名中提取版本号
     * 例如: home-center-pc-agent-1.1.jar -> 1.1
     * 如果无法提取则使用默认版本号
     */
    private String extractVersion(String filename) {
        // 去掉 .jar 后缀
        String name = filename.substring(0, filename.length() - 4);
        // 尝试匹配最后一个横线后的版本号（如 1.0, 1.1, 2.0.0 等）
        int lastDash = name.lastIndexOf('-');
        if (lastDash > 0) {
            String candidate = name.substring(lastDash + 1);
            if (candidate.matches("\\d+(\\.\\d+)*")) {
                return candidate;
            }
        }
        return CURRENT_VERSION;
    }
}
