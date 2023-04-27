package com.wwh.home.center.service.impl;

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
        UserInfo record = new UserInfo();
        record.setPhone(phone);
        record.setDeleted(false);
        List<UserInfo> list = userInfoMapper.select(record);
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
        UserInfo record = new UserInfo();
        record.setPhone(phone);
        record.setDeleted(false);
        return userInfoMapper.selectCount(record) > 0;
    }

    /**
     * 如果是之前老的可以考虑用这种方式<br>
     * 之前用的 com.tuliu.common.tk.interceptor.PageInterceptor 效率比较低
     */
    // @Override
    public PageInfo<UserInfoVo> findUserPage(UserQuery query, PageInfo<UserInfoVo> page) {

        // 开始分页
        PageHelper.startPage(page.getPageNumber(), page.getPageSize());

        List<UserInfo> list = userInfoMapper.findUserPage(query, page);
        List<UserInfoVo> voList = convert(list);
        page.setData(voList);

        // 总记录数
        page.setTotalCount((int) ((Page<?>) list).getTotal());
        // 总页数
        page.setMaxPageNumber(((Page<?>) list).getPages());
        return page;
    }

    /**
     * 如果是全新的可以考虑用这种不用xml文件的写法
     */
    public PageInfo<UserInfoVo> findUserPage2(UserQuery query, PageInfo<UserInfoVo> page) {
        Example example = new Example(UserInfo.class);
        Example.Criteria criteria = example.createCriteria();
        // 注意：条件查询使用的是属性名
        if (query.getGender() != null) {
            criteria.andEqualTo("gender", query.getGender());
        }
        if (StringUtils.isNotBlank(query.getUsername())) {
            criteria.andLike("username", "%" + query.getUsername() + "%");
        }
        if (query.getLocked() != null) {
            criteria.andEqualTo("locked", query.getLocked());
        }
        if (query.getCreateBy() != null) {
            criteria.andEqualTo("createBy", query.getCreateBy());
        }
        example.orderBy("userId").desc();
        // example.setOrderByClause("user_id DESC");

        PageHelper.startPage(page.getPageNumber(), page.getPageSize());
        List<UserInfo> list = userInfoMapper.selectByExample(example);
        List<UserInfoVo> voList = convert(list);
        page.setData(voList);

        // 总记录数
        page.setTotalCount((int) ((Page<?>) list).getTotal());
        // 总页数
        page.setMaxPageNumber(((Page<?>) list).getPages());

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
        PageHelper.startPage(page, size);
        List<UserInfo> list = userInfoMapper.unionAllTest();
        List<UserInfoVo> voList = convert(list);

        PageInfo<UserInfoVo> pageInfo = new PageInfo<UserInfoVo>(page, size);
        pageInfo.setData(voList);
        pageInfo.setTotalCount((int) ((Page<?>) list).getTotal());
        return pageInfo;
    }

}
