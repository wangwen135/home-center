package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.TodoItem;

import java.util.List;

/**
 * 用户待办事项 Service
 *
 * <p>所有操作均按当前登录用户隔离；删除为软删除（不物理删除）。</p>
 *
 * @author wangwh
 */
public interface TodoItemService {

    /**
     * 当前用户的全部待办（未删除）。
     */
    List<TodoItem> listCurrentUser();

    /**
     * 新增待办，返回带 id 的实体。
     */
    TodoItem addCurrentUser(TodoItem item);

    /**
     * 修改待办（内容/优先级/标签/完成状态）。
     */
    void updateCurrentUser(TodoItem item);

    /**
     * 切换完成状态，返回切换后的实体。
     */
    TodoItem toggleCompleted(Long id);

    /**
     * 软删除单条。
     */
    void deleteCurrent(Long id);

    /**
     * 软删除多条，返回受影响条数。
     */
    int batchDeleteCurrent(List<Long> ids);

    /**
     * 批量修改优先级，返回受影响条数。
     */
    int batchUpdatePriorityCurrent(List<Long> ids, String priority);

    /**
     * 清除当前用户已完成的待办（软删除），返回清除条数。
     */
    int clearCompletedCurrent();

    /**
     * 导出当前用户的全部待办（用于备份）。
     */
    List<TodoItem> exportCurrentUser();

    /**
     * 导入待办（按内容去重），返回实际导入条数。
     */
    int importCurrentUser(List<TodoItem> items);
}
