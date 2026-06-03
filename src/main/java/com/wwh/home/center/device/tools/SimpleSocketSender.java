package com.wwh.home.center.device.tools;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * socket 发报机
 *
 * @author wangwh
 * @date 2024/06/03
 */
@Slf4j
public class SimpleSocketSender {

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
        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

            log.debug("发送指令：{}", command);
            out.println(command);

            // 读取服务器响应
            String response = in.readLine();
            log.debug("收到响应：{}", response);

            return response;
        }
    }
}
