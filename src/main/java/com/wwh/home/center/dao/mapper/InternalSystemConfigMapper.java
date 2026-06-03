package com.wwh.home.center.dao.mapper;

import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 内部系统配置 Mapper 接口
 * </p>
 *
 * @author wwh
 * @since 2024-01-24
 */
@Repository
public interface InternalSystemConfigMapper extends BaseMapper<InternalSystemConfig> {

    List<InternalSystemConfig> getInternalSystemByUserId(Integer userId);
}
