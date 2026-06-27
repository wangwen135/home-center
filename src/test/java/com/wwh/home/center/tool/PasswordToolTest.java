package com.wwh.home.center.tool;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PasswordTool} 单元测试。
 *
 * <p>关键不变量：哈希公式必须与生产代码 {@code UserServiceImpl#encryptPassword} /
 * {@code LoginManager#matchPwd} 一致 —— {@code sha256(password + salt + password)}，
 * 否则工具生成的密码写入库后将无法登录。
 *
 * @author wangwh
 * @date 2026/06/25
 */
class PasswordToolTest {

    private static final String PWD = "123456";

    @Test
    void generateSaltProducesSixDigits() {
        String salt = PasswordTool.generateSalt();

        assertEquals(6, salt.length());
        assertTrue(salt.matches("\\d{6}"), "盐应为 6 位纯数字");
    }

    @Test
    void hashMatchesSystemAlgorithm() {
        String salt = "482913";

        // 与 UserServiceImpl#encryptPassword / LoginManager#matchPwd 完全一致的公式：
        // sha256(明文 + 盐 + 明文)，明文前后各夹一次盐
        String expected = DigestUtils.sha256Hex(PWD + salt + PWD);

        assertEquals(expected, PasswordTool.hash(PWD, salt));
    }

    @Test
    void generateProducesMatchingSaltAndHash() {
        String[] pair = PasswordTool.generate(PWD);

        assertEquals(2, pair.length);
        assertTrue(PasswordTool.verify(PWD, pair[0], pair[1]),
                "generate 产出的 salt+hash 必须能被 verify 校验通过");
    }

    @Test
    void samePasswordYieldsDifferentSalt() {
        String[] a = PasswordTool.generate(PWD);
        String[] b = PasswordTool.generate(PWD);

        assertNotEquals(a[0], b[0], "同一明文两次生成的盐应不同");
        assertNotEquals(a[1], b[1]);
        // 但都应能校验通过
        assertTrue(PasswordTool.verify(PWD, a[0], a[1]));
        assertTrue(PasswordTool.verify(PWD, b[0], b[1]));
    }

    @Test
    void verifyRejectsWrongPasswordAndNullHash() {
        String[] pair = PasswordTool.generate(PWD);

        assertFalse(PasswordTool.verify("wrong", pair[0], pair[1]));
        assertFalse(PasswordTool.verify(PWD, pair[0], null));
        assertFalse(PasswordTool.verify(PWD, pair[0], "deadbeef"));
    }
}
