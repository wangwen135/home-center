package com.wwh.home.center.device.tools;

import com.alibaba.fastjson2.JSON;
import com.wwh.home.center.model.CmdResult;
import com.wwh.home.center.model.entity.PcDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * socket 发报机
 *
 * @author wangwh
 * @date 2024/06/03
 */
@Slf4j
@Component
public class SimpleSocketSender {

    @Value("${agent.access-token:${access.token:}}")
    private String accessToken;

    @Value("${agent.connect-timeout-seconds:5}")
    private int connectTimeoutSeconds;

    @Value("${agent.socket-timeout-seconds:10}")
    private int socketTimeoutSeconds;

    /**
     * 发送普通单行指令
     */
    public String sendCommand(PcDevice device, String command) throws IOException {
        return sendCommand(device.getIpAddress(), device.getSocketPort(), command,
                accessToken, connectTimeoutSeconds, socketTimeoutSeconds);
    }

    /**
     * 执行远程命令
     */
    public CmdResult executeCommand(PcDevice device, String command, int timeoutSeconds) throws IOException {
        int processTimeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : socketTimeoutSeconds;
        int readTimeoutSeconds = processTimeoutSeconds + socketTimeoutSeconds;
        return executeCommand(device.getIpAddress(), device.getSocketPort(), command, processTimeoutSeconds,
                accessToken, connectTimeoutSeconds, readTimeoutSeconds);
    }

    /**
     * 发送指令
     *
     * @param hostname 主机名
     * @param port 端口号
     * @param command 命令
     * @return 响应结果
     * @throws UnknownHostException 未知主机异常
     * @throws IOException IO异常
     */
    public static String sendCommand(String hostname, int port, String command) throws UnknownHostException, IOException {
        return sendCommand(hostname, port, command, "", 5, 10);
    }

    public static String sendCommand(String hostname, int port, String command, String accessToken,
                                     int connectTimeoutSeconds, int socketTimeoutSeconds) throws IOException {
        try (Socket socket = connect(hostname, port, connectTimeoutSeconds, socketTimeoutSeconds);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            auth(out, in, accessToken);

            log.debug("发送指令：{}", command);
            writeLine(out, command);

            // 读取服务器响应
            String response = readLine(in);
            log.debug("收到响应：{}", response);

            return response;
        }
    }

    public static CmdResult executeCommand(String hostname, int port, String command, int timeoutSeconds,
                                           String accessToken, int connectTimeoutSeconds,
                                           int socketTimeoutSeconds) throws IOException {
        try (Socket socket = connect(hostname, port, connectTimeoutSeconds, socketTimeoutSeconds);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            auth(out, in, accessToken);

            Map<String, Object> payload = new HashMap<>();
            payload.put("command", command);
            payload.put("timeoutSeconds", timeoutSeconds);
            byte[] payloadBytes = JSON.toJSONString(payload).getBytes(StandardCharsets.UTF_8);

            log.debug("发送远程命令：{}", command);
            writeLine(out, "CMD " + payloadBytes.length);
            out.write(payloadBytes);
            out.flush();

            String header = readLine(in);
            if (header == null || !header.startsWith("CMD-RESULT ")) {
                throw new IOException("远程命令响应协议错误: " + header);
            }

            int length = parseLength(header, "CMD-RESULT ");
            byte[] resultBytes = new byte[length];
            in.readFully(resultBytes);
            String resultJson = new String(resultBytes, StandardCharsets.UTF_8);
            log.debug("收到远程命令结果：{}", resultJson);
            return JSON.parseObject(resultJson, CmdResult.class);
        }
    }

    private static Socket connect(String hostname, int port, int connectTimeoutSeconds, int socketTimeoutSeconds)
            throws IOException {
        Socket socket = new Socket();
        int connectTimeoutMillis = Math.max(connectTimeoutSeconds, 1) * 1000;
        int socketTimeoutMillis = Math.max(socketTimeoutSeconds, 1) * 1000;
        socket.connect(new InetSocketAddress(hostname, port), connectTimeoutMillis);
        socket.setSoTimeout(socketTimeoutMillis);
        return socket;
    }

    private static void auth(DataOutputStream out, DataInputStream in, String accessToken) throws IOException {
        writeLine(out, "AUTH " + (accessToken == null ? "" : accessToken));
        String response = readLine(in);
        if (!"AUTH OK".equals(response)) {
            throw new IOException("PC Agent认证失败: " + response);
        }
    }

    private static void writeLine(DataOutputStream out, String line) throws IOException {
        out.write(line.getBytes(StandardCharsets.UTF_8));
        out.write('\n');
        out.flush();
    }

    private static String readLine(DataInputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            int value = in.read();
            if (value == -1) {
                if (buffer.size() == 0) {
                    return null;
                }
                break;
            }
            if (value == '\n') {
                break;
            }
            if (value != '\r') {
                buffer.write(value);
            }
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private static int parseLength(String header, String prefix) throws IOException {
        try {
            return Integer.parseInt(header.substring(prefix.length()).trim());
        } catch (NumberFormatException e) {
            throw new IOException("响应长度格式错误: " + header, e);
        }
    }
}
