package com.wwh.home.center.device;

import com.wwh.home.center.device.tools.WakeOnLan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * PC的电源事件处理
 *
 * @author wangwh
 * @date 2024/05/31
 */
@Slf4j
@Component
public class PcPowerEventProcessor {

    private String ipAddress = "255.255.255.255"; //书房台式机真实的IP地址是：localhost，这里用广播地址
    private String macAddress = "00-00-00-00-00-00";

    public void handlePowerOn() {
        log.info("## 启动电脑...");
        try {
            WakeOnLan.sendWakeOnLanPacket(macAddress, ipAddress);
            log.info("## 唤醒数据包已经发送");
        } catch (Exception e) {
            log.error("发送Wake-on-LAN 数据包唤醒机器异常", e);
        }
    }

    public void handlePowerOff() {
        log.info("## 关闭电脑...");

        //连续发送 10 个wol 数据包
        try {
            for (int i = 0; i < 10; i++) {
                WakeOnLan.sendWakeOnLanPacket(macAddress, ipAddress);
                log.info("## 关机数据包已经发送");
            }
        } catch (Exception e) {
            log.error("发送Wake-on-LAN 数据包关闭机器异常", e);
        }
    }

}
