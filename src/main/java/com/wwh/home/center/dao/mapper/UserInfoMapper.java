package com.wwh.home.center.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wwh.home.center.common.model.PageInfo;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.qo.UserQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    List<UserInfo> findUserPage(Page<?> page, @Param("query") UserQuery query);

    /**
     * 查多个用户的信息
     *
     * @param ids
     * @return
     */
    List<UserInfo> listUser(@Param("list") List<Long> ids);

    /**
     * 测试 UNION ALL 的分页
     *
     * @return
     */
    List<UserInfo> unionAllTest(Page page);

}
