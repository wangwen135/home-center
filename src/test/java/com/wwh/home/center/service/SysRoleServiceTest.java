package com.wwh.home.center.service;

import com.wwh.home.center.HomeCenterApp;
import com.wwh.home.center.model.entity.SysRole;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * 角色服务测试
 *
 * @author wangwh
 * @date 2024/01/10
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HomeCenterApp.class)
public class SysRoleServiceTest {

    @Autowired
    private SysRoleService sysRoleService;

    @Test
    public void testGetRoleByUserId() {
        int userId = 1;
        SysRole sysRole = sysRoleService.getRoleByUserId(userId);
        log.debug("角色信息：{}", sysRole);
        assertNotNull(sysRole);
    }
}
