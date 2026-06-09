package com.wwh.home.center.controller.device;

import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import com.wwh.home.center.device.tools.SimpleSocketSender;
import com.wwh.home.center.model.common.ApiResponse;
import com.wwh.home.center.model.entity.PcDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PcMonitorControllerTest {

    @TempDir
    Path screenshotDir;

    private PcDeviceMapper pcDeviceMapper;
    private SimpleSocketSender simpleSocketSender;
    private PcMonitorController controller;

    @BeforeEach
    void 初始化控制器依赖() {
        pcDeviceMapper = mock(PcDeviceMapper.class);
        simpleSocketSender = mock(SimpleSocketSender.class);
        controller = new PcMonitorController();
        ReflectionTestUtils.setField(controller, "pcDeviceMapper", pcDeviceMapper);
        ReflectionTestUtils.setField(controller, "simpleSocketSender", simpleSocketSender);
        ReflectionTestUtils.setField(controller, "screenshotDir", screenshotDir.toString());
        ReflectionTestUtils.setField(controller, "screenshotWaitSeconds", 1);
    }

    @Test
    void 触发截图后能找到最新截图() throws Exception {
        PcDevice device = enabledDevice();
        when(pcDeviceMapper.selectById(8L)).thenReturn(device);
        Path screenshot = screenshotDir.resolve("device-8-screenshot-001.png");
        Files.write(screenshot, "png".getBytes(StandardCharsets.UTF_8));

        ApiResponse<Map<String, String>> response = controller.captureScreenshot(8L);

        assertEquals("success", response.getStatus());
        assertEquals("截图已更新", response.getMessage());
        assertEquals("/device/pc/8/screenshot/latest", response.getData().get("url"));
        assertEquals(screenshot.toString(), response.getData().get("path"));
        verify(simpleSocketSender).sendCommand(device, "screenshot");
    }

    @Test
    void 截图超时时返回错误() throws Exception {
        PcDevice device = enabledDevice();
        when(pcDeviceMapper.selectById(9L)).thenReturn(device);

        ApiResponse<Map<String, String>> response = controller.captureScreenshot(9L);

        assertEquals("error", response.getStatus());
        assertEquals("已发送截图指令，但未等到截图上传", response.getMessage());
        verify(simpleSocketSender).sendCommand(device, "screenshot");
    }

    @Test
    void 设备禁用时不触发截图指令() throws Exception {
        PcDevice disabled = enabledDevice();
        disabled.setStatus(0);
        when(pcDeviceMapper.selectById(10L)).thenReturn(disabled);

        ApiResponse<Map<String, String>> response = controller.captureScreenshot(10L);

        assertEquals("error", response.getStatus());
        assertEquals("设备不存在或已禁用", response.getMessage());
        verify(simpleSocketSender, never()).sendCommand(disabled, "screenshot");
    }

    @Test
    void 获取最新截图优先返回设备专属文件() throws Exception {
        PcDevice device = enabledDevice();
        when(pcDeviceMapper.selectById(8L)).thenReturn(device);
        Files.write(screenshotDir.resolve("screenshot-old.png"), "old".getBytes(StandardCharsets.UTF_8));
        Path deviceScreenshot = screenshotDir.resolve("device-8-screenshot-new.png");
        Files.write(deviceScreenshot, "new".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<Resource> response = controller.getLatestScreenshot(8L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(deviceScreenshot.toFile(), response.getBody().getFile());
    }

    @Test
    void 没有截图或设备不可用时返回404() {
        when(pcDeviceMapper.selectById(11L)).thenReturn(enabledDevice());

        ResponseEntity<Resource> response = controller.getLatestScreenshot(11L);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody() == null);
    }

    private PcDevice enabledDevice() {
        PcDevice device = new PcDevice();
        device.setId(8L);
        device.setIpAddress("127.0.0.1");
        device.setSocketPort(9000);
        device.setStatus(1);
        return device;
    }
}
