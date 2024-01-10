package com.wwh.home.center.service;


import com.wwh.home.center.common.model.PageInfo;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.qo.UserQuery;
import com.wwh.home.center.model.vo.UserInfoVo;

import java.util.List;

public interface UserService {

    List<UserInfo> getUserByNameOrPhone(String identity);

    UserInfo getById(Long userId);

    UserInfo getByPhone(String phone);

    /**
     * 电话号码是否存在
     *
     * @param phone
     * @return
     */
    boolean existPhone(String phone);

    /**
     * 用户分页查询
     *
     * @param query
     * @param page
     * @return
     */
    PageInfo<UserInfoVo> findUserPage(UserQuery query, PageInfo<UserInfoVo> page);

    /**
     * 根据ID集合查用户
     *
     * @param ids
     * @return
     */
    List<UserInfoVo> listUser(List<Long> ids);

    /**
     * Union All 分页测试
     *
     * @param page
     * @param size
     * @return
     */
    PageInfo<UserInfoVo> unionAllTest(int page, int size);
}
