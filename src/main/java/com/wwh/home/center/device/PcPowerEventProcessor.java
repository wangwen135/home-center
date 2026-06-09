package com.wwh.home.center.device;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.home.center.device.tools.SimpleSocketSender;
import com.wwh.home.center.device.tools.WakeOnLan;
import com.wwh.home.center.model.entity.PcDevice;
import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class PcPowerEventProcessor {

    @Value("${device.pc.broadcast-address:255.255.255.255}")
    private String broadcastAddress;

    @Autowired
    private PcDeviceMapper pcDeviceMapper;

    @Autowired
    private SimpleSocketSender simpleSocketSender;

    public void handlePowerOnAll() throws Exception {
        List<PcDevice> devices = listDevices();
        for (PcDevice device : devices) {
            handlePowerOn(device.getId());
        }
    }

    public void handlePowerOffAll() throws Exception {
        List<PcDevice> devices = listDevices();
        for (PcDevice device : devices) {
            handlePowerOff(device.getId());
        }
    }

    /**
     * 启动指定电脑
     *
     * @throws Exception 操作异常
     */
    public void handlePowerOn(Long deviceId) throws Exception {
        PcDevice device = pcDeviceMapper.selectById(deviceId);
        if (device == null || device.getStatus() != 1) {
            throw new IllegalArgumentException("设备不存在或已禁用");
        }

        try {
            WakeOnLan.sendWakeOnLanPacket(device.getMacAddress(), broadcastAddress);
            log.info("## 已发送唤醒数据包到设备: {}, MAC地址: {}", device.getName(), device.getMacAddress());
        } catch (Exception e) {
            String error = String.format("发送Wake-on-LAN数据包唤醒设备[%s]异常", device.getName());
            log.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * 关闭指定电脑
     *
     * @throws Exception 操作异常
     */
    public void handlePowerOff(Long deviceId) throws Exception {
        PcDevice device = pcDeviceMapper.selectById(deviceId);
        if (device == null || device.getStatus() != 1) {
            throw new IllegalArgumentException("设备不存在或已禁用");
        }

        try {
            String response = simpleSocketSender.sendCommand(device, "shutdown");
            log.info("## 已发送关机指令到设备: {}, IP: {}, 响应: {}",
                    device.getName(), device.getIpAddress(), response);
        } catch (Exception e) {
            String error = String.format("发送关机指令到设备[%s]异常：%s", device.getName(), e.getMessage());
            log.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * 异步启动指定电脑
     */
    public void handlePowerOnAsync(Long deviceId) {
        CompletableFuture.runAsync(() -> {
            try {
                handlePowerOn(deviceId);
            } catch (Exception e) {
                log.error("异步启动设备[{}]异常", deviceId, e);
            }
        });
    }

    /**
     * 异步关闭指定电脑
     */
    public void handlePowerOffAsync(Long deviceId) {
        CompletableFuture.runAsync(() -> {
            try {
                handlePowerOff(deviceId);
            } catch (Exception e) {
                log.error("异步关闭设备[{}]异常", deviceId, e);
            }
        });
    }

    /**
     * 异步启动所有电脑
     */
    public void handlePowerOnAllAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                handlePowerOnAll();
            } catch (Exception e) {
                log.error("异步启动所有设备异常", e);
            }
        });
    }

    /**
     * 异步关闭所有电脑
     */
    public void handlePowerOffAllAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                handlePowerOffAll();
            } catch (Exception e) {
                log.error("异步关闭所有设备异常", e);
            }
        });
    }

    /**
     * 获取所有可用设备
     */
    public List<PcDevice> listDevices() {
        return pcDeviceMapper.selectList(
                new LambdaQueryWrapper<PcDevice>()
                        .eq(PcDevice::getStatus, 1)
                        .orderByAsc(PcDevice::getId)
        );
    }
}
