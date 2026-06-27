package com.wwh.home.center.tool;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.SecureRandom;
import java.util.Scanner;

/**
 * 密码生成 / 校验测试工具。
 *
 * <p>本系统密码存储算法（与生产代码保持一致，改动需三处同步）：
 * <ul>
 *   <li>盐：6 位随机数字字符串（每位 {@code 0-9}）</li>
 *   <li>哈希：{@code sha256(password + salt + password)}（明文前后各夹一次盐）</li>
 * </ul>
 * 权威实现见 {@code UserServiceImpl#encryptPassword / #randomSalt} 与
 * {@code LoginManager#matchPwd}。本工具用于开发/运维场景手动生成可写入
 * {@code user_info} 表的 {@code salt}+{@code password}，以及排查登录密码问题。
 *
 * <h3>用法</h3>
 * <pre>
 *   # 交互式生成（明文不进 shell 历史 / 进程列表）
 *   java -cp &lt;testClasspath&gt; com.wwh.home.center.tool.PasswordTool
 *
 *   # 直接生成指定明文
 *   java ...PasswordTool generate 123456
 *
 *   # 校验明文 + 盐 是否匹配库中哈希
 *   java ...PasswordTool verify 123456 482913 a1b2c3...
 * </pre>
 *
 * @author wangwh
 * @date 2026/06/25
 */
public final class PasswordTool {

    /** 与 UserServiceImpl.SALT_LENGTH 保持一致 */
    private static final int SALT_LENGTH = 6;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordTool() {
    }

    /**
     * 生成 6 位随机数字盐，算法与 {@code UserServiceImpl#randomSalt} 一致。
     */
    public static String generateSalt() {
        StringBuilder sb = new StringBuilder(SALT_LENGTH);
        for (int i = 0; i < SALT_LENGTH; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 计算密码哈希。算法 {@code sha256(password + salt + password)}，
     * 与 {@code UserServiceImpl#encryptPassword} / {@code LoginManager#matchPwd} 一致。
     */
    public static String hash(String password, String salt) {
        return DigestUtils.sha256Hex(password + salt + password);
    }

    /**
     * 生成 {@code [salt, hash]} 对，可直接写入 {@code user_info} 表。
     */
    public static String[] generate(String password) {
        String salt = generateSalt();
        return new String[]{salt, hash(password, salt)};
    }

    /**
     * 校验明文 + 盐 是否匹配库中存储的哈希。
     */
    public static boolean verify(String password, String salt, String storedHash) {
        return storedHash != null && hash(password, salt).equals(storedHash);
    }

    public static void main(String[] args) {
        String mode = args.length == 0 ? "generate" : args[0];
        switch (mode) {
            case "generate": {
                String pwd = args.length >= 2 ? args[1] : readPassword();
                String[] r = generate(pwd);
                printGenerated(pwd, r[0], r[1]);
                break;
            }
            case "verify": {
                if (args.length < 4) {
                    usage();
                    return;
                }
                boolean ok = verify(args[1], args[2], args[3]);
                System.out.println(ok ? "[OK] 匹配" : "[FAIL] 不匹配");
                break;
            }
            default:
                usage();
        }
    }

    private static void printGenerated(String pwd, String salt, String hash) {
        System.out.println("明文   : " + pwd);
        System.out.println("salt   : " + salt);
        System.out.println("hash   : " + hash);
        System.out.println();
        System.out.println("-- 更新 user_info 表（替换 <userId>）：");
        System.out.printf("UPDATE user_info SET salt='%s', password='%s', update_time=NOW() WHERE id=<userId>;%n",
                salt, hash);
    }

    private static String readPassword() {
        System.out.print("请输入明文密码（回车确认）：");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static void usage() {
        System.out.println("用法：");
        System.out.println("  PasswordTool                             交互式生成密码哈希");
        System.out.println("  PasswordTool generate <pwd>              生成指定明文的 salt + hash");
        System.out.println("  PasswordTool verify <pwd> <salt> <hash>  校验是否匹配");
    }
}
