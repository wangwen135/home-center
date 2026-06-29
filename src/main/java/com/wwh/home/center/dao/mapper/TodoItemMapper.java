package com.wwh.home.center.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wwh.home.center.model.entity.TodoItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户待办事项 Mapper
 *
 * @author wangwh
 */
@Mapper
public interface TodoItemMapper extends BaseMapper<TodoItem> {
}
