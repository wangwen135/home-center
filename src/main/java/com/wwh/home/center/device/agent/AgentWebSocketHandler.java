package com.wwh.home.center.device.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class AgentWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private AgentConnectionManager agentConnectionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("PC Agent WebSocket connected: {}", session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        agentConnectionManager.handleMessage(session, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        agentConnectionManager.unregister(session);
        log.info("PC Agent WebSocket closed: {}, status={}", session.getRemoteAddress(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("PC Agent WebSocket transport error: {}", session.getRemoteAddress(), exception);
        agentConnectionManager.unregister(session);
    }
}
