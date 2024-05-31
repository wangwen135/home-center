package com.wwh.home.center.config.mqtt.bemfa;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

/**
 * 发送消息
 *
 * @author wangwh
 * @date 2024/05/31
 */
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface BfMqttGateway {

    @Gateway(requestChannel = "mqttOutboundChannel")
    void sendToMqtt(String data, @Header("mqtt_topic") String topic);
}
