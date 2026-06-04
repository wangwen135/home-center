package com.wwh.home.center.security;

import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TokenManagerTest {

    @Test
    void generatesValidTokenAndRemovesIt() {
        LoggedUserAllInfo userAllInfo = buildLoggedUser();

        String token = TokenManager.generateToken(userAllInfo);

        assertNotNull(token);
        assertTrue(TokenManager.isValidToken(token));
        assertSame(userAllInfo, TokenManager.getUserAllInfoFromToken(token));

        TokenManager.removeToken(token);
        assertFalse(TokenManager.isValidToken(token));
        assertNull(TokenManager.getUserAllInfoFromToken(token));
    }

    @Test
    void handlesBlankToken() {
        TokenManager.removeToken("");
        TokenManager.refreshToken("");

        assertFalse(TokenManager.isValidToken(""));
        assertNull(TokenManager.getUserAllInfoFromToken(""));
    }

    @Test
    void rejectsNullUserInfo() {
        assertThrows(IllegalArgumentException.class, () -> TokenManager.generateToken(null));
    }

    private LoggedUserAllInfo buildLoggedUser() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1);
        userInfo.setUsername("tester");

        SysRole sysRole = new SysRole();
        sysRole.setId(1);
        sysRole.setName("admin");

        return new LoggedUserAllInfo(userInfo, sysRole, Collections.emptyList(), Collections.emptyList());
    }
}
