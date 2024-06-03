package com.wwh.home.center.device;

import com.wwh.home.center.device.tools.SimpleSocketSender;
import com.wwh.home.center.device.tools.WakeOnLan;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * PC的电源事件处理
 *
 * @author wangwh
 * @date 2024/05/31
 */
@Slf4j
@Component
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "device.pc")
public class PcPowerEventProcessor {

    // 广播地址：255.255.255.255
    private String broadcastAddress = "255.255.255.255";

    //书房台式机真实的IP地址是：localhost
    private String ipAddress = "localhost";

    // MAC地址
    private String macAddress = "00-00-00-00-00-00";

    // 端口
    private int socketPort = 65432;

    /**
     * 启动电脑
     */
    public void handlePowerOn() {
        log.info("## 启动电脑...");
        try {
            WakeOnLan.sendWakeOnLanPacket(macAddress, broadcastAddress);
            log.info("## 唤醒数据包已经发送");
        } catch (Exception e) {
            log.error("发送Wake-on-LAN 数据包唤醒机器异常", e);
        }
    }

    /**
     * 关闭电脑
     */
    public void handlePowerOff() {
        log.info("## 关闭电脑...");
        try {
            String response = SimpleSocketSender.sendCommand(ipAddress, socketPort, "shutdown");
            log.info("## 关机指令已经发送，收到响应：{}", response);
        } catch (Exception e) {
            log.error("发送关闭电脑指令异常", e);
        }
    }

    /**
     * 这种方式，如果电脑没有启动，发送这个指令会导致其关机
     */
    public void handlePowerOff2() {
        log.info("## 关闭电脑...");

        //连续发送 10 个wol 数据包
        try {
            for (int i = 0; i < 10; i++) {
                WakeOnLan.sendWakeOnLanPacket(macAddress, broadcastAddress);
                log.info("## 关机数据包已经发送");
            }
        } catch (Exception e) {
            log.error("发送Wake-on-LAN 数据包关闭机器异常", e);
        }
    }
}
