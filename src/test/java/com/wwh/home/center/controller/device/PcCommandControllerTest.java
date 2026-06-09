package com.wwh.home.center.controller.device;

import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import com.wwh.home.center.device.tools.SimpleSocketSender;
import com.wwh.home.center.model.CmdResult;
import com.wwh.home.center.model.common.ApiResponse;
import com.wwh.home.center.model.entity.PcDevice;
import com.wwh.home.center.model.qo.PcCommandRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PcCommandControllerTest {

    private PcDeviceMapper pcDeviceMapper;
    private SimpleSocketSender simpleSocketSender;
    private PcCommandController controller;

    @BeforeEach
    void 初始化控制器依赖() {
        pcDeviceMapper = mock(PcDeviceMapper.class);
        simpleSocketSender = mock(SimpleSocketSender.class);
        controller = new PcCommandController();
        ReflectionTestUtils.setField(controller, "pcDeviceMapper", pcDeviceMapper);
        ReflectionTestUtils.setField(controller, "simpleSocketSender", simpleSocketSender);
        ReflectionTestUtils.setField(controller, "defaultCommandTimeoutSeconds", 30);
    }

    @Test
    void 远程命令执行成功并使用请求超时时间() throws Exception {
        PcDevice device = enabledDevice();
        PcCommandRequest request = new PcCommandRequest();
        request.setCommand("dir");
        request.setTimeoutSeconds(5);
        CmdResult cmdResult = new CmdResult();
        cmdResult.setCommand("dir");
        cmdResult.setSuccess(true);
        cmdResult.setExitCode(0);
        when(pcDeviceMapper.selectById(1L)).thenReturn(device);
        when(simpleSocketSender.executeCommand(device, "dir", 5)).thenReturn(cmdResult);

        ApiResponse<CmdResult> response = controller.executeCommand(1L, request);

        assertEquals("success", response.getStatus());
        assertEquals("命令执行完成", response.getMessage());
        assertSame(cmdResult, response.getData());
        verify(simpleSocketSender).executeCommand(device, "dir", 5);
    }

    @Test
    void 未指定超时时间时使用默认配置() throws Exception {
        PcDevice device = enabledDevice();
        PcCommandRequest request = new PcCommandRequest();
        request.setCommand("whoami");
        when(pcDeviceMapper.selectById(2L)).thenReturn(device);
        when(simpleSocketSender.executeCommand(eq(device), eq("whoami"), eq(30))).thenReturn(new CmdResult());

        ApiResponse<CmdResult> response = controller.executeCommand(2L, request);

        assertEquals("success", response.getStatus());
        verify(simpleSocketSender).executeCommand(device, "whoami", 30);
    }

    @Test
    void 空命令直接返回错误且不查询设备() throws Exception {
        PcCommandRequest request = new PcCommandRequest();
        request.setCommand("  ");

        ApiResponse<CmdResult> response = controller.executeCommand(1L, request);

        assertEquals("error", response.getStatus());
        assertEquals("命令不能为空", response.getMessage());
        verify(pcDeviceMapper, never()).selectById(1L);
        verify(simpleSocketSender, never()).executeCommand(eq(enabledDevice()), eq(""), eq(1));
    }

    @Test
    void 设备不存在或禁用时返回错误() throws Exception {
        PcCommandRequest request = new PcCommandRequest();
        request.setCommand("dir");
        PcDevice disabled = enabledDevice();
        disabled.setStatus(0);
        when(pcDeviceMapper.selectById(3L)).thenReturn(disabled);

        ApiResponse<CmdResult> response = controller.executeCommand(3L, request);

        assertEquals("error", response.getStatus());
        assertEquals("设备不存在或已禁用", response.getMessage());
        verify(simpleSocketSender, never()).executeCommand(eq(disabled), eq("dir"), eq(30));
    }

    @Test
    void Socket连接失败时返回异常信息() throws Exception {
        PcDevice device = enabledDevice();
        PcCommandRequest request = new PcCommandRequest();
        request.setCommand("dir");
        when(pcDeviceMapper.selectById(4L)).thenReturn(device);
        when(simpleSocketSender.executeCommand(device, "dir", 30)).thenThrow(new IOException("连接失败"));

        ApiResponse<CmdResult> response = controller.executeCommand(4L, request);

        assertEquals("error", response.getStatus());
        assertTrue(response.getMessage().contains("连接失败"));
    }

    private PcDevice enabledDevice() {
        PcDevice device = new PcDevice();
        device.setId(1L);
        device.setIpAddress("127.0.0.1");
        device.setSocketPort(9000);
        device.setStatus(1);
        return device;
    }
}
