package com.wwh.home.center.device.tools;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Wake-on-LAN（WoL）工具
 *
 * @author wangwh
 * @date 2024/05/31
 */
public class WakeOnLan {

    private static final int PORT = 9;

    /**
     * 发送wake on lan 数据包
     *
     * @param macAddress
     * @param ipAddress
     * @throws Exception
     */
    public static void sendWakeOnLanPacket(String macAddress, String ipAddress) throws Exception {
        byte[] macBytes = getMacBytes(macAddress);
        byte[] bytes = new byte[6 + 16 * macBytes.length];

        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }

        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        InetAddress address = InetAddress.getByName(ipAddress);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        socket.close();
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("[:-]");

        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }

        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }

        return bytes;
    }
}
