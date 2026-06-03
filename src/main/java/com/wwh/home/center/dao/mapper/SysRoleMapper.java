package com.wwh.home.center.dao.mapper;

import com.wwh.home.center.model.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author wwh
 * @since 2024-01-08
 */
@Repository
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysRole> getRolesByUserId(Integer userId);
}
