package com.wwh.home.center.device.tools;

import com.alibaba.fastjson2.JSON;
import com.wwh.home.center.model.CmdResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleSocketSenderTest {

    @Test
    void AUTH握手成功时写入认证帧() throws Exception {
        Socket socket = mockSocket("AUTH OK\n");
        ByteArrayOutputStream output = (ByteArrayOutputStream) socket.getOutputStream();
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(output);

        invokeAuth(out, in, "secret-token");

        assertEquals("AUTH secret-token\n", output.toString(StandardCharsets.UTF_8.name()));
    }

    @Test
    void AUTH握手失败时抛出IOException() throws Exception {
        Socket socket = mockSocket("AUTH DENIED\n");
        ByteArrayOutputStream output = (ByteArrayOutputStream) socket.getOutputStream();
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(output);

        IOException exception = assertThrows(IOException.class, () -> invokeAuth(out, in, "bad-token"));

        assertTrue(exception.getMessage().contains("PC Agent认证失败"));
        assertEquals("AUTH bad-token\n", output.toString(StandardCharsets.UTF_8.name()));
    }

    @Test
    void 远程命令CMD帧包含长度和JSON载荷() throws Exception {
        Socket socket = mockSocket("");
        ByteArrayOutputStream output = (ByteArrayOutputStream) socket.getOutputStream();
        DataOutputStream out = new DataOutputStream(output);
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "ipconfig");
        payload.put("timeoutSeconds", 3);
        byte[] payloadBytes = JSON.toJSONString(payload).getBytes(StandardCharsets.UTF_8);

        invokeWriteLine(out, "CMD " + payloadBytes.length);
        out.write(payloadBytes);
        out.flush();

        byte[] frame = output.toByteArray();
        String header = new String(frame, 0, ("CMD " + payloadBytes.length + "\n").length(), StandardCharsets.UTF_8);
        String body = new String(frame, header.length(), frame.length - header.length(), StandardCharsets.UTF_8);
        assertEquals("CMD " + payloadBytes.length + "\n", header);
        assertTrue(body.contains("\"command\":\"ipconfig\""));
        assertTrue(body.contains("\"timeoutSeconds\":3"));
    }

    @Test
    void CMD结果帧可以按长度解析为命令结果() throws Exception {
        CmdResult expected = new CmdResult();
        expected.setCommand("dir");
        expected.setSuccess(true);
        expected.setExitCode(0);
        expected.setStdout("ok");
        byte[] resultBytes = JSON.toJSONString(expected).getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        response.write(("CMD-RESULT " + resultBytes.length + "\n").getBytes(StandardCharsets.UTF_8));
        response.write(resultBytes);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.toByteArray()));

        String header = invokeReadLine(in);
        int length = invokeParseLength(header, "CMD-RESULT ");
        byte[] body = new byte[length];
        in.readFully(body);
        CmdResult actual = JSON.parseObject(new String(body, StandardCharsets.UTF_8), CmdResult.class);

        assertEquals("dir", actual.getCommand());
        assertTrue(actual.getSuccess());
        assertEquals(0, actual.getExitCode());
        assertEquals("ok", actual.getStdout());
    }

    @Test
    void CMD结果长度格式错误时抛出IOException() {
        IOException exception = assertThrows(IOException.class, () -> invokeParseLength("CMD-RESULT abc", "CMD-RESULT "));

        assertTrue(exception.getMessage().contains("响应长度格式错误"));
    }

    @Test
    void 连接失败异常可作为Socket发送失败场景断言() {
        assertThrows(IOException.class, () -> {
            throw new ConnectException("连接失败");
        });
    }

    private Socket mockSocket(String input) throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        return socket;
    }

    private void invokeAuth(DataOutputStream out, DataInputStream in, String accessToken) throws Exception {
        try {
            ReflectionTestUtils.invokeMethod(SimpleSocketSender.class, "auth", out, in, accessToken);
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        } catch (UndeclaredThrowableException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }

    private void invokeWriteLine(DataOutputStream out, String line) {
        ReflectionTestUtils.invokeMethod(SimpleSocketSender.class, "writeLine", out, line);
    }

    private String invokeReadLine(DataInputStream in) {
        return ReflectionTestUtils.invokeMethod(SimpleSocketSender.class, "readLine", in);
    }

    private int invokeParseLength(String header, String prefix) throws IOException {
        try {
            Integer length = ReflectionTestUtils.invokeMethod(SimpleSocketSender.class, "parseLength", header, prefix);
            return length;
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        } catch (UndeclaredThrowableException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }
}
