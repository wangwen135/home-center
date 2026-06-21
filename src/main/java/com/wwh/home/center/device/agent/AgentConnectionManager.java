package com.wwh.home.center.device.agent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import com.wwh.home.center.model.CmdResult;
import com.wwh.home.center.model.entity.PcDevice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class AgentConnectionManager {

    private static final String SESSION_DEVICE_ID = "deviceId";

    @Autowired
    private PcDeviceMapper pcDeviceMapper;

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<JSONObject>> pendingRequests = new ConcurrentHashMap<>();

    public void handleMessage(WebSocketSession session, String payload) throws IOException {
        JSONObject message = JSON.parseObject(payload);
        String type = message.getString("type");
        if ("REGISTER".equals(type)) {
            register(session, message);
        } else if ("RESULT".equals(type)) {
            completeRequest(message);
        } else if ("PONG".equals(type)) {
            touch(session);
        } else {
            log.warn("Unsupported agent message type: {}", type);
        }
    }

    public void unregister(WebSocketSession session) {
        Object deviceId = session.getAttributes().get(SESSION_DEVICE_ID);
        if (deviceId instanceof Long) {
            sessions.remove((Long) deviceId, session);
        }
    }

    public boolean isOnline(Long deviceId) {
        WebSocketSession session = sessions.get(deviceId);
        return session != null && session.isOpen();
    }

    public String sendCommand(PcDevice device, String command, int timeoutSeconds) throws Exception {
        JSONObject result = send(device.getId(), "COMMAND", command, timeoutSeconds, null);
        if (Boolean.FALSE.equals(result.getBoolean("success"))) {
            String error = result.getString("error");
            return StringUtils.defaultIfBlank(error, "failed");
        }
        return StringUtils.defaultIfBlank(result.getString("message"), "OK");
    }

    public CmdResult executeCommand(PcDevice device, String command, int timeoutSeconds) throws Exception {
        JSONObject result = send(device.getId(), "EXEC", command, timeoutSeconds, timeoutSeconds);
        CmdResult cmdResult = result.toJavaObject(CmdResult.class);
        cmdResult.setCommand(command);
        return cmdResult;
    }

    private void register(WebSocketSession session, JSONObject message) throws IOException {
        PcDevice device = resolveDevice(session, message);
        Long oldDeviceId = (Long) session.getAttributes().put(SESSION_DEVICE_ID, device.getId());
        if (oldDeviceId != null && !oldDeviceId.equals(device.getId())) {
            sessions.remove(oldDeviceId, session);
        }

        WebSocketSession oldSession = sessions.put(device.getId(), session);
        if (oldSession != null && oldSession != session && oldSession.isOpen()) {
            oldSession.close();
        }

        JSONObject response = new JSONObject();
        response.put("type", "REGISTERED");
        response.put("deviceId", device.getId());
        response.put("requestId", message.getString("requestId"));
        session.sendMessage(new TextMessage(response.toJSONString()));
        log.info("PC Agent registered: deviceId={}, name={}, agentId={}",
                device.getId(), device.getName(), device.getAgentId());
    }

    private PcDevice resolveDevice(WebSocketSession session, JSONObject message) {
        String agentId = StringUtils.trimToNull(message.getString("agentId"));
        List<String> macAddresses = readMacAddresses(message.getJSONArray("macAddresses"));

        PcDevice device = null;
        if (agentId != null) {
            device = pcDeviceMapper.selectOne(new LambdaQueryWrapper<PcDevice>()
                    .eq(PcDevice::getAgentId, agentId)
                    .last("limit 1"));
        }
        if (device == null && !macAddresses.isEmpty()) {
            List<PcDevice> matches = pcDeviceMapper.selectList(new LambdaQueryWrapper<PcDevice>()
                    .in(PcDevice::getMacAddress, macAddresses));
            if (matches.size() == 1) {
                device = matches.get(0);
            } else if (matches.size() > 1) {
                log.warn("Multiple devices matched agent MACs, create new device instead: {}", macAddresses);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (device == null) {
            device = new PcDevice();
            device.setCreateTime(now);
            device.setStatus(1);
        }

        applyAgentInfo(device, session, message, macAddresses, now);

        if (device.getId() == null) {
            pcDeviceMapper.insert(device);
        } else {
            pcDeviceMapper.updateById(device);
        }
        return device;
    }

    private void applyAgentInfo(PcDevice device, WebSocketSession session, JSONObject message,
                                List<String> macAddresses, LocalDateTime now) {
        String agentId = StringUtils.trimToNull(message.getString("agentId"));
        String name = StringUtils.trimToNull(message.getString("name"));
        String hostname = StringUtils.trimToNull(message.getString("hostname"));
        String osName = StringUtils.trimToNull(message.getString("osName"));
        String agentVersion = StringUtils.trimToNull(message.getString("agentVersion"));

        if (StringUtils.isBlank(device.getName())) {
            device.setName(StringUtils.defaultIfBlank(name,
                    StringUtils.defaultIfBlank(hostname, agentId == null ? "unknown-agent" : agentId.substring(0, Math.min(8, agentId.length())))));
        }
        if (StringUtils.isBlank(device.getAgentId())) {
            device.setAgentId(agentId);
        }
        device.setHostname(hostname);
        device.setOsName(osName);
        device.setAgentVersion(agentVersion);
        device.setIpAddress(remoteAddress(session));
        if (!macAddresses.isEmpty() && StringUtils.isBlank(device.getMacAddress())) {
            device.setMacAddress(macAddresses.get(0));
        }
        device.setLastSeenTime(now);
        device.setUpdateTime(now);
    }

    private JSONObject send(Long deviceId, String type, String command, int waitTimeoutSeconds, Integer processTimeoutSeconds) throws Exception {
        WebSocketSession session = sessions.get(deviceId);
        if (session == null || !session.isOpen()) {
            throw new IOException("PC Agent offline: deviceId=" + deviceId);
        }

        String requestId = UUID.randomUUID().toString();
        JSONObject request = new JSONObject();
        request.put("type", type);
        request.put("requestId", requestId);
        request.put("command", command);
        if (processTimeoutSeconds != null) {
            request.put("timeoutSeconds", processTimeoutSeconds);
        }

        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(request.toJSONString()));
            }
            return future.get(Math.max(waitTimeoutSeconds, 1), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new IOException("PC Agent command timeout: deviceId=" + deviceId + ", command=" + command, e);
        } finally {
            pendingRequests.remove(requestId);
        }
    }

    private void completeRequest(JSONObject message) {
        String requestId = message.getString("requestId");
        CompletableFuture<JSONObject> future = pendingRequests.get(requestId);
        if (future == null) {
            log.warn("No pending agent request for result: {}", requestId);
            return;
        }
        future.complete(message);
    }

    private void touch(WebSocketSession session) {
        Object deviceId = session.getAttributes().get(SESSION_DEVICE_ID);
        if (deviceId instanceof Long) {
            PcDevice device = new PcDevice();
            device.setId((Long) deviceId);
            device.setLastSeenTime(LocalDateTime.now());
            device.setUpdateTime(LocalDateTime.now());
            pcDeviceMapper.updateById(device);
        }
    }

    private List<String> readMacAddresses(JSONArray values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (int i = 0; i < values.size(); i++) {
            String mac = StringUtils.trimToNull(values.getString(i));
            if (mac != null) {
                result.add(mac);
            }
        }
        return result;
    }

    private String remoteAddress(WebSocketSession session) {
        InetSocketAddress address = session.getRemoteAddress();
        if (address == null || address.getAddress() == null) {
            return null;
        }
        return address.getAddress().getHostAddress();
    }
}
