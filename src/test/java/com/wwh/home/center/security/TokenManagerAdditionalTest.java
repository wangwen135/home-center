package com.wwh.home.center.security;

import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.vo.TokenVo;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenManagerAdditionalTest {

    @Test
    void 生成Token后可以解析用户并返回脱敏列表() {
        LoggedUserAllInfo user = buildLoggedUser();
        String token = TokenManager.generateToken(user);

        assertSame(user, TokenManager.getUserAllInfoFromToken(token));
        List<TokenVo> tokens = TokenManager.getAll();
        assertTrue(tokens.stream().anyMatch(x -> x.getToken().startsWith("**********")));

        TokenManager.removeToken(token);
        assertFalse(TokenManager.isValidToken(token));
    }

    @Test
    @SuppressWarnings("unchecked")
    void 清理过期Token后无法继续解析() {
        LoggedUserAllInfo user = buildLoggedUser();
        String token = TokenManager.generateToken(user);
        Map<String, Object> tokenMap = (Map<String, Object>) ReflectionTestUtils.getField(TokenManager.class, "tokenMap");
        Object tokenInfo = tokenMap.get(token);
        assertNotNull(tokenInfo);
        ReflectionTestUtils.setField(tokenInfo, "expirationTime", System.currentTimeMillis() - 1000L);

        new TokenManager().cleanExpiredToken();

        assertNull(TokenManager.getUserAllInfoFromToken(token));
        assertEquals(null, tokenMap.get(token));
    }

    private LoggedUserAllInfo buildLoggedUser() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(12);
        userInfo.setUsername("token-user");

        SysRole role = new SysRole();
        role.setId(2);
        role.setName("operator");
        return new LoggedUserAllInfo(userInfo, role, Collections.emptyList(), Collections.emptyList());
    }
}
