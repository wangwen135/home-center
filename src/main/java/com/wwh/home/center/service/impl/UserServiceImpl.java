package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.common.model.PageInfo;
import com.wwh.home.center.common.util.PageHelper;
import com.wwh.home.center.dao.mapper.SysRoleMapper;
import com.wwh.home.center.dao.mapper.UserInfoMapper;
import com.wwh.home.center.dao.mapper.UserRoleMapper;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.entity.UserRole;
import com.wwh.home.center.model.qo.UserQuery;
import com.wwh.home.center.model.vo.UserInfoVo;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务
 *
 * @author wangwh
 * @date 2021-12-15
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private static final int SALT_LENGTH = 6;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public List<UserInfo> getUserByNameOrPhone(String identity) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(w -> w.eq("username", identity).or().eq("phone", identity))
                .eq("deleted", 0);
        return userInfoMapper.selectList(queryWrapper);
    }

    @Override
    public UserInfo getById(Long userId) {
        UserInfo user = userInfoMapper.selectById(userId);
        return user;
    }

    @Override
    public UserInfo getByPhone(String phone) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserInfo::getPhone, phone).eq(UserInfo::getDeleted, false);
        List<UserInfo> list = userInfoMapper.selectList(queryWrapper);
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            log.warn("电话号码：{} 出现重复数据", phone);
        }
        return list.get(0);

    }

    @Override
    public boolean existPhone(String phone) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone).eq("deleted", false);
        return userInfoMapper.selectCount(queryWrapper) > 0;
    }

    //写xml文件的方式
    @Override
    public PageInfo<UserInfoVo> findUserPage(UserQuery query, PageInfo<UserInfoVo> page) {
        Page<UserInfo> mPage = new Page(page.getPageNum(), page.getPageSize());
        List<UserInfo> list = userInfoMapper.findUserPage(mPage, query);
        List<UserInfoVo> voList = convert(list);
        page.setData(voList);
        // 总记录数
        page.setTotal(mPage.getTotal());
        return page;
    }

    /**
     * 如果是全新的可以考虑用这种不用xml文件的写法
     */
    public PageInfo<UserInfoVo> findUserPage2(UserQuery query, PageInfo<UserInfoVo> page) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();

        // 注意：条件查询使用的是属性名
        if (query.getGender() != null) {
            queryWrapper.eq("gender", query.getGender());
        }
        if (StringUtils.isNotBlank(query.getUsername())) {
            //like（完全模糊，即“like '%val%'”）
            queryWrapper.like("username", query.getUsername());
        }
        if (query.getLocked() != null) {
            queryWrapper.eq("locked", query.getLocked());
        }
        if (query.getCreateBy() != null) {
            queryWrapper.eq("createBy", query.getCreateBy());
        }
        queryWrapper.orderByDesc("userId");

        Page p = userInfoMapper.selectPage(PageHelper.pageInfo2MybatisPlusPage(page), queryWrapper);
        List<UserInfo> list = p.getRecords();

        List<UserInfoVo> voList = convert(list);
        page.setData(voList);
        // 总记录数
        page.setTotal(p.getTotal());

        return page;
    }

    @Override
    public List<UserInfoVo> listUser(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserInfo> list = userInfoMapper.listUser(ids);
        return convert(list);
    }

    private List<UserInfoVo> convert(List<UserInfo> list) {
        List<UserInfoVo> voList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return voList;
        }
        list.forEach(ubi -> {
            UserInfoVo vo = new UserInfoVo();
            BeanUtils.copyProperties(ubi, vo);
            voList.add(vo);
        });
        fillRoleNames(voList);
        return voList;
    }

    private void fillRoleNames(List<UserInfoVo> voList) {
        Set<Integer> userIds = voList.stream().map(UserInfoVo::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        QueryWrapper<UserRole> userRoleWrapper = new QueryWrapper<>();
        userRoleWrapper.in("user_id", userIds);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        if (userRoles == null || userRoles.isEmpty()) {
            return;
        }
        Set<Integer> roleIds = userRoles.stream().map(UserRole::getRoleId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, String> roleNameMap = Collections.emptyMap();
        if (!roleIds.isEmpty()) {
            QueryWrapper<SysRole> roleWrapper = new QueryWrapper<>();
            roleWrapper.in("id", roleIds).eq("deleted", 0);
            roleNameMap = sysRoleMapper.selectList(roleWrapper).stream()
                    .collect(Collectors.toMap(SysRole::getId, SysRole::getName, (a, b) -> a));
        }
        Map<Integer, Integer> userRoleMap = userRoles.stream()
                .collect(Collectors.toMap(UserRole::getUserId, UserRole::getRoleId, (a, b) -> a));
        Map<Integer, String> finalRoleNameMap = roleNameMap;
        voList.forEach(vo -> vo.setRoleName(finalRoleNameMap.get(userRoleMap.get(vo.getId()))));
    }

    @Override
    public PageInfo<UserInfoVo> unionAllTest(int page, int size) {
        // Union All 查询 测试分页插件
        Page mPage = new Page(page, size);
        List<UserInfo> list = userInfoMapper.unionAllTest(mPage);
        List<UserInfoVo> voList = convert(list);

        PageInfo<UserInfoVo> pageInfo = new PageInfo<UserInfoVo>(page, size);
        pageInfo.setData(voList);
        pageInfo.setTotal(mPage.getTotal());
        return pageInfo;
    }

    @Override
    public void resetPassword(Long userId) {
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BusinessException("用户不存在");
        }
        updatePassword(user, DEFAULT_PASSWORD);
        log.info("管理员重置用户密码成功，userId={}", userId);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Integer userId = UserContextHolder.isLoggedIn();
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BusinessException("当前用户不存在");
        }
        String oldPasswordHex = encryptPassword(oldPassword, user.getSalt());
        if (!oldPasswordHex.equals(user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        updatePassword(user, newPassword);
        log.info("用户修改自己的密码成功，userId={}", userId);
    }

    private void updatePassword(UserInfo user, String password) {
        String salt = randomSalt();
        UserInfo update = new UserInfo();
        update.setId(user.getId());
        update.setSalt(salt);
        update.setPassword(encryptPassword(password, salt));
        update.setUpdateBy(UserContextHolder.getUserId());
        update.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(update);
    }

    private String encryptPassword(String password, String salt) {
        return DigestUtils.sha256Hex(password + salt + password);
    }

    private String randomSalt() {
        StringBuilder sb = new StringBuilder(SALT_LENGTH);
        for (int i = 0; i < SALT_LENGTH; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    @Override
    public List<Integer> getUserRoleIds(Long userId) {
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", toIntegerUserId(userId));
        List<UserRole> list = userRoleMapper.selectList(queryWrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, Integer roleId) {
        QueryWrapper<UserRole> deleteWrapper = Wrappers.query();
        Integer intUserId = toIntegerUserId(userId);
        deleteWrapper.eq("user_id", intUserId);
        userRoleMapper.delete(deleteWrapper);
        UserRole userRole = new UserRole();
        userRole.setUserId(intUserId);
        userRole.setRoleId(roleId);
        userRoleMapper.insert(userRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserInfo user) {
        if (StringUtils.isBlank(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (StringUtils.isBlank(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (user.getRoleId() == null) {
            throw new BusinessException("角色不能为空");
        }
        QueryWrapper<UserInfo> existWrapper = new QueryWrapper<>();
        existWrapper.eq("username", user.getUsername()).eq("deleted", 0);
        if (userInfoMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        String salt = randomSalt();
        user.setId(null);
        user.setSalt(salt);
        user.setPassword(encryptPassword(user.getPassword(), salt));
        user.setDisabled(false);
        user.setLocked(false);
        user.setExpired(false);
        user.setDeleted(false);
        user.setCreateBy(UserContextHolder.getUserId());
        user.setCreateTime(LocalDateTime.now());
        userInfoMapper.insert(user);
        assignRoles(Long.valueOf(user.getId()), user.getRoleId());
    }

    @Override
    public void updateUser(UserInfo user) {
        UserInfo update = new UserInfo();
        update.setId(user.getId());
        update.setNickname(user.getNickname());
        update.setPhone(user.getPhone());
        update.setUpdateBy(UserContextHolder.getUserId());
        update.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(update);
    }

    @Override
    public void toggleUserStatus(Long userId, Boolean disabled) {
        if (Long.valueOf(SysConstants.SUPER_ADMIN_ROLE_ID).equals(userId)) {
            throw new BusinessException("超级管理员不可禁用");
        }
        UserInfo update = new UserInfo();
        update.setId(toIntegerUserId(userId));
        update.setDisabled(disabled);
        update.setUpdateBy(UserContextHolder.getUserId());
        update.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(update);
    }

    private Integer toIntegerUserId(Long userId) {
        if (userId == null || userId > Integer.MAX_VALUE || userId < Integer.MIN_VALUE) {
            throw new BusinessException("用户ID不合法");
        }
        return userId.intValue();
    }
}
