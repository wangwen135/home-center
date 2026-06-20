package com.wwh.home.center.service;


import com.wwh.home.center.common.model.PageInfo;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.qo.UserProfileRequest;
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

    /**
     * 管理员重置用户密码
     *
     * @param userId 用户ID
     */
    void resetPassword(Long userId);

    /**
     * 当前登录用户修改自己的密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String oldPassword, String newPassword);

    List<Integer> getUserRoleIds(Long userId);

    void assignRoles(Long userId, Integer roleId);

    void createUser(UserInfo user);

    void updateUser(UserInfo user);

    /**
     * 当前登录用户修改自己的资料（昵称、性别、手机、邮箱）
     *
     * @param userId  当前用户ID
     * @param request 待更新字段
     */
    void updateProfile(Integer userId, UserProfileRequest request);

    /**
     * 更新用户头像
     *
     * @param userId  用户ID
     * @param avatar  头像相对路径
     */
    void updateAvatar(Integer userId, String avatar);

    void toggleUserStatus(Long userId, Boolean disabled);
}
