package com.wwh.home.center.controller.common;

import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.TodoItem;
import com.wwh.home.center.service.TodoItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户待办事项接口
 *
 * <p>所有接口按当前登录用户隔离，登录即可访问（权限点 /api/todo/** 默认授予所有角色）。</p>
 *
 * @author wangwh
 */
@Slf4j
@Validated
@Api(tags = "待办事项接口")
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    @Autowired
    private TodoItemService todoItemService;

    @ApiOperation("当前用户全部待办")
    @GetMapping("/list")
    public Result<List<TodoItem>> list() {
        return Result.success(todoItemService.listCurrentUser());
    }

    @ApiOperation("新增待办")
    @PostMapping
    public Result<TodoItem> add(@RequestBody TodoItem item) {
        return Result.success(todoItemService.addCurrentUser(item));
    }

    @ApiOperation("修改待办")
    @PutMapping
    public Result<Void> update(@RequestBody TodoItem item) {
        todoItemService.updateCurrentUser(item);
        return Result.success();
    }

    @ApiOperation("切换完成状态")
    @PutMapping("/{id}/toggle")
    public Result<TodoItem> toggle(@PathVariable @NotNull(message = "待办ID不能为空") Long id) {
        return Result.success(todoItemService.toggleCompleted(id));
    }

    @ApiOperation("软删除单条待办")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "待办ID不能为空") Long id) {
        todoItemService.deleteCurrent(id);
        return Result.success();
    }

    @ApiOperation("批量软删除待办")
    @PostMapping("/batch/delete")
    public Result<Integer> batchDelete(@RequestBody BatchIdsRequest request) {
        return Result.success(todoItemService.batchDeleteCurrent(request == null ? null : request.getIds()));
    }

    @ApiOperation("批量修改优先级")
    @PutMapping("/batch/priority")
    public Result<Integer> batchPriority(@RequestBody BatchPriorityRequest request) {
        List<Long> ids = request == null ? null : request.getIds();
        String priority = request == null ? null : request.getPriority();
        return Result.success(todoItemService.batchUpdatePriorityCurrent(ids, priority));
    }

    @ApiOperation("清除已完成的待办")
    @PostMapping("/clear-completed")
    public Result<Integer> clearCompleted() {
        return Result.success(todoItemService.clearCompletedCurrent());
    }

    @ApiOperation("导出当前用户全部待办")
    @GetMapping("/export")
    public Result<List<TodoItem>> export() {
        return Result.success(todoItemService.exportCurrentUser());
    }

    @ApiOperation("导入待办（按内容去重）")
    @PostMapping("/import")
    public Result<Integer> importTodos(@RequestBody List<TodoItem> items) {
        return Result.success(todoItemService.importCurrentUser(items));
    }

    @Data
    public static class BatchIdsRequest {
        private List<Long> ids;
    }

    @Data
    public static class BatchPriorityRequest {
        private List<Long> ids;
        private String priority;
    }
}
