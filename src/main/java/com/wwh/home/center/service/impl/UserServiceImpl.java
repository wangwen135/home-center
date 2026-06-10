package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.common.model.PageInfo;
import com.wwh.home.center.common.util.PageHelper;
import com.wwh.home.center.dao.mapper.UserInfoMapper;
import com.wwh.home.center.model.entity.UserInfo;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        return voList;
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

}
