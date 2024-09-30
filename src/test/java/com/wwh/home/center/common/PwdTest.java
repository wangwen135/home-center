package com.wwh.home.center.common;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

/**
 * TODO
 *
 * @author wangwh
 * @date 2024/01/11
 */
public class PwdTest {
    public static void main(String[] args) {
        String salt = UUID.randomUUID().toString().replace("-", "");
        System.out.println("salt:");
        System.out.println(salt);
        String pwd = "123456";
        System.out.println("pwd:");
        System.out.println(pwd);

        String hexPwd = DigestUtils.md5Hex(pwd + salt);
        DigestUtils.sha256Hex(pwd + salt + pwd);
        System.out.println("encrypt pwd:");
        System.out.println(hexPwd);

    }
}
