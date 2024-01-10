package com.wwh.home.center.common.util;


import com.wwh.home.center.common.constant.SysConstants;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

/**
 * AES加密工具
 *
 * @author wangwh
 * @date 2023/05/31
 */
public class AESUtils {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_PADDING_MODE = "AES/ECB/PKCS5Padding";

    public static byte[] encryptStr(String plaintext, String key) throws Exception {
        Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE);
        return cipher.doFinal(plaintext.getBytes(SysConstants.DEFAULT_CHARACTER_ENCODING));
    }

    public static byte[] encrypt(byte[] plaintext, String key) throws Exception {
        Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE);
        return cipher.doFinal(plaintext);
    }

    public static String decryptStr(byte[] ciphertext, String key) throws Exception {
        Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE);
        byte[] decryptedBytes = cipher.doFinal(ciphertext);
        return new String(decryptedBytes, SysConstants.DEFAULT_CHARACTER_ENCODING);
    }

    public static byte[] decrypt(byte[] ciphertext, String key) throws Exception {
        Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE);
        return cipher.doFinal(ciphertext);
    }

    public static Cipher getCipher(String key, int encryptMode) throws Exception {
        SecretKeySpec secretKey = generateKey(key);
        Cipher cipher = Cipher.getInstance(AES_PADDING_MODE);
        cipher.init(encryptMode, secretKey);
        return cipher;
    }

    public static SecretKeySpec generateKey(String key) throws Exception {
        byte[] keyBytes = key.getBytes(SysConstants.DEFAULT_CHARACTER_ENCODING);
        MessageDigest md = MessageDigest.getInstance("MD5");
        return new SecretKeySpec(md.digest(keyBytes), AES_ALGORITHM);
    }

    /**
     * <pre>
     * 计算加密后的字节长度
     * 只计算使用AES/ECB/PKCS5Padding模式加密后的长度
     * 分组长度为16字节
     * </pre>
     *
     * @param originalLength
     * @return
     */
    public static int calcEncryptedLength(int originalLength) {
        int blockSize = 16;
        int paddingSize = blockSize - (originalLength % blockSize);
        return originalLength + paddingSize;
    }
}
