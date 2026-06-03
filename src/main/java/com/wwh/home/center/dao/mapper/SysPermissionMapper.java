package com.wwh.home.center.dao.mapper;

import com.wwh.home.center.model.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 权限表 Mapper 接口
 * </p>
 *
 * @author wwh
 * @since 2024-01-08
 */
@Repository
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    List<SysPermission> getPermissionByRoleId(Integer roleId);
}
