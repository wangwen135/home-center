package com.wwh.home.center.socket;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * TODO
 *
 * @author wangwh
 * @date 2024/06/03
 */
public class SimpleSocketClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 65432;

        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

            System.out.println("发送 :hello 指令给服务器");
            out.println("hello");

            // 读取服务器响应
            String response = in.readLine();
            System.out.println("服务器响应: " + response);

        } catch (UnknownHostException e) {
            System.err.println("未知主机: " + hostname);
        } catch (IOException e) {
            System.err.println("I/O 错误: " + e.getMessage());
        }
        System.out.println("结束！");
    }
}
