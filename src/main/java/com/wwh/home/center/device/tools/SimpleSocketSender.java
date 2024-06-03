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
     * @param hostname
     * @param port
     * @param command
     * @return
     */
    public static String sendCommand(String hostname, int port, String command) {
        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

            log.debug("发送指令：{}", command);
            out.println(command);

            // 读取服务器响应
            String response = in.readLine();
            log.debug("收到响应：{}", response);

            return response;
        } catch (UnknownHostException e) {
            log.error("未知主机:{}", hostname, e);
            return "err: 未知主机";
        } catch (IOException e) {
            log.error("I/O 错误", e);
            return "err: I/O 错误";
        }
    }
}
