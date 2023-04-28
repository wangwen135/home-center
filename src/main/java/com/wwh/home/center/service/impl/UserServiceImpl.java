package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wwh.home.center.common.model.PageInfo;
import com.wwh.home.center.dao.mapper.UserInfoMapper;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.qo.UserQuery;
import com.wwh.home.center.model.vo.UserInfoVo;
import com.wwh.home.center.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;


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

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Cacheable(cacheNames = "userIdCache", key = "#userId")
    @Override
    public UserInfo getById(Long userId) {
        UserInfo user = userInfoMapper.selectById(userId);
        return user;
    }

    @Override
    @Cacheable(cacheNames = "getByPhone", key = "#phone")
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
    @Cacheable(cacheNames = "userPhoneCache", key = "#phone")
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

        Page p = userInfoMapper.selectPage(page.getMybatisPlusPage(), queryWrapper);
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

}
