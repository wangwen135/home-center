package com.wwh.home.center.config.mqtt.bemfa;

import com.wwh.home.center.device.PcPowerEventProcessor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * 巴法云MQTT配置
 *
 * @author wangwh
 * @date 2024/05/30
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mqtt.bemfa")
public class BfMqttConfig {
    /**
     * 服务器地址
     */
    private String bfServerUri = "tcp://bemfa.com:9501";
    /**
     * 私钥
     */
    private String bfSecurityKey = "change-me";
    /**
     * 主题
     */
    private String pcTopic = "PC001";

    @Autowired
    private PcPowerEventProcessor pcPowerEventProcessor;

    // 配置 MQTT 客户端工厂
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{bfServerUri});
        options.setUserName(""); // 如果需要用户名密码
        options.setPassword("".toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    // 定义输入通道
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 配置 MQTT 消息驱动适配器
    @Bean
    public MessageProducer inbound() {

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(bfSecurityKey, mqttClientFactory(),
                        pcTopic); //用户私钥   主题
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 配置消息处理器
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
            String receiveMsg = message.getPayload().toString();
            log.info("收到巴法云云平台的MQTT消息：topic={}  msg={}", topic, receiveMsg);

            if ("on".equalsIgnoreCase(receiveMsg)) {
                handlePowerOn();
            } else if ("off".equalsIgnoreCase(receiveMsg)) {
                handlePowerOff();
            } else {
                log.warn("收到【巴法】topic={} 的无效消息：{}", topic, receiveMsg);
            }
        };
    }


    //ID先固定这两个
    private void handlePowerOn() {
        pcPowerEventProcessor.handlePowerOnAsync(1L);
        pcPowerEventProcessor.handlePowerOnAsync(2L);
    }

    private void handlePowerOff() {
        pcPowerEventProcessor.handlePowerOffAsync(1L);
        pcPowerEventProcessor.handlePowerOffAsync(2L);

    }

    // 定义输出通道
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    // 配置 MQTT 消息发送器
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(bfSecurityKey, mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(pcTopic); // 默认主题，如果发送消息时没有指定
        return messageHandler;
    }
}
