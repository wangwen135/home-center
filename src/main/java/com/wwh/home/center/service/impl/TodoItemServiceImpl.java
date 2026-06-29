package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.dao.mapper.TodoItemMapper;
import com.wwh.home.center.model.entity.TodoItem;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.TodoItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户待办事项 Service 实现
 *
 * <p>查询恒带 userId + deleted=false；删除为软删除（置 deleted=true，不物理删除）。</p>
 *
 * @author wangwh
 */
@Slf4j
@Service
public class TodoItemServiceImpl implements TodoItemService {

    private static final String DEFAULT_PRIORITY = "low";
    private static final Set<String> VALID_PRIORITY = new HashSet<>(Arrays.asList("high", "medium", "low"));

    private static final int MAX_CONTENT = 500;
    private static final int MAX_TAGS = 15;
    private static final int MAX_TAG_LEN = 30;
    private static final int MAX_BATCH_IDS = 200;
    private static final int MAX_IMPORT_ITEMS = 1000;

    @Autowired
    private TodoItemMapper todoItemMapper;

    @Override
    public List<TodoItem> listCurrentUser() {
        return todoItemMapper.selectList(activeWrapper());
    }

    @Override
    public TodoItem addCurrentUser(TodoItem item) {
        if (item == null) {
            throw new BusinessException("待办内容不能为空");
        }
        Integer userId = currentUserId();
        LocalDateTime now = LocalDateTime.now();
        item.setId(null);
        item.setUserId(userId);
        item.setContent(normalizeContent(item.getContent()));
        item.setPriority(resolvePriority(item.getPriority(), false));
        item.setCompleted(Boolean.TRUE.equals(item.getCompleted()));
        item.setTags(normalizeTags(item.getTags()));
        item.setDeleted(false);
        item.setCreateTime(now);
        item.setUpdateTime(now);
        todoItemMapper.insert(item);
        log.info("新增待办成功，userId={}, id={}", userId, item.getId());
        return item;
    }

    @Override
    public void updateCurrentUser(TodoItem item) {
        if (item == null || item.getId() == null) {
            throw new BusinessException("待办ID不能为空");
        }
        Integer userId = currentUserId();
        TodoItem update = new TodoItem();
        update.setContent(normalizeContent(item.getContent()));
        update.setPriority(resolvePriority(item.getPriority(), false));
        update.setTags(normalizeTags(item.getTags()));
        update.setCompleted(Boolean.TRUE.equals(item.getCompleted()));
        update.setUpdateTime(LocalDateTime.now());
        int rows = todoItemMapper.update(update, new LambdaUpdateWrapper<TodoItem>()
                .eq(TodoItem::getId, item.getId())
                .eq(TodoItem::getUserId, userId)
                .eq(TodoItem::getDeleted, false));
        if (rows == 0) {
            throw new BusinessException("待办不存在");
        }
        log.info("更新待办成功，userId={}, id={}", userId, item.getId());
    }

    @Override
    public TodoItem toggleCompleted(Long id) {
        if (id == null) {
            throw new BusinessException("待办ID不能为空");
        }
        Integer userId = currentUserId();
        TodoItem item = todoItemMapper.selectOne(new LambdaQueryWrapper<TodoItem>()
                .eq(TodoItem::getId, id)
                .eq(TodoItem::getUserId, userId)
                .eq(TodoItem::getDeleted, false));
        if (item == null) {
            throw new BusinessException("待办不存在");
        }
        boolean next = !Boolean.TRUE.equals(item.getCompleted());
        todoItemMapper.update(null, new LambdaUpdateWrapper<TodoItem>()
                .set(TodoItem::getCompleted, next)
                .set(TodoItem::getUpdateTime, LocalDateTime.now())
                .eq(TodoItem::getId, id)
                .eq(TodoItem::getUserId, userId));
        item.setCompleted(next);
        return item;
    }

    @Override
    public void deleteCurrent(Long id) {
        if (id == null) {
            throw new BusinessException("待办ID不能为空");
        }
        Integer userId = currentUserId();
        int rows = softDelete(new LambdaUpdateWrapper<TodoItem>()
                .eq(TodoItem::getId, id)
                .eq(TodoItem::getUserId, userId));
        if (rows == 0) {
            throw new BusinessException("待办不存在");
        }
        log.info("软删除待办成功，userId={}, id={}", userId, id);
    }

    @Override
    public int batchDeleteCurrent(List<Long> ids) {
        List<Long> normalizedIds = normalizeIds(ids);
        if (normalizedIds.isEmpty()) {
            return 0;
        }
        Integer userId = currentUserId();
        int rows = softDelete(new LambdaUpdateWrapper<TodoItem>()
                .eq(TodoItem::getUserId, userId)
                .in(TodoItem::getId, normalizedIds));
        log.info("批量软删除待办，userId={}, count={}", userId, rows);
        return rows;
    }

    @Override
    public int batchUpdatePriorityCurrent(List<Long> ids, String priority) {
        List<Long> normalizedIds = normalizeIds(ids);
        if (normalizedIds.isEmpty()) {
            return 0;
        }
        Integer userId = currentUserId();
        String resolved = resolvePriority(priority, false);
        int rows = todoItemMapper.update(null, new LambdaUpdateWrapper<TodoItem>()
                .set(TodoItem::getPriority, resolved)
                .set(TodoItem::getUpdateTime, LocalDateTime.now())
                .eq(TodoItem::getUserId, userId)
                .eq(TodoItem::getDeleted, false)
                .in(TodoItem::getId, normalizedIds));
        log.info("批量修改优先级，userId={}, count={}", userId, rows);
        return rows;
    }

    @Override
    public int clearCompletedCurrent() {
        Integer userId = currentUserId();
        int rows = softDelete(new LambdaUpdateWrapper<TodoItem>()
                .eq(TodoItem::getUserId, userId)
                .eq(TodoItem::getCompleted, true));
        log.info("清除已完成待办，userId={}, count={}", userId, rows);
        return rows;
    }

    @Override
    public List<TodoItem> exportCurrentUser() {
        return listCurrentUser();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importCurrentUser(List<TodoItem> items) {
        Integer userId = currentUserId();
        if (items == null || items.isEmpty()) {
            return 0;
        }
        if (items.size() > MAX_IMPORT_ITEMS) {
            throw new BusinessException("单次最多导入" + MAX_IMPORT_ITEMS + "条待办");
        }
        // 按内容去重：已存在（未删除）的内容跳过
        Set<String> existing = listCurrentUser().stream()
                .map(TodoItem::getContent)
                .collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (TodoItem src : items) {
            if (src == null) {
                continue;
            }
            String content = contentOrNull(src.getContent());
            if (content == null || existing.contains(content)) {
                continue;
            }
            existing.add(content);
            TodoItem item = new TodoItem();
            item.setUserId(userId);
            item.setContent(content);
            item.setPriority(resolvePriority(src.getPriority(), true));
            item.setCompleted(Boolean.TRUE.equals(src.getCompleted()));
            item.setTags(normalizeTags(src.getTags()));
            item.setDeleted(false);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            todoItemMapper.insert(item);
            count++;
        }
        log.info("导入待办，userId={}, imported={}", userId, count);
        return count;
    }

    // ==================== 私有辅助 ====================

    private LambdaQueryWrapper<TodoItem> activeWrapper() {
        return new LambdaQueryWrapper<TodoItem>()
                .eq(TodoItem::getUserId, currentUserId())
                .eq(TodoItem::getDeleted, false)
                .orderByDesc(TodoItem::getUpdateTime)
                .orderByDesc(TodoItem::getId);
    }

    /**
     * 软删除（置 deleted=true 并刷新 updateTime），返回受影响条数。
     */
    private int softDelete(LambdaUpdateWrapper<TodoItem> wrapper) {
        wrapper.set(TodoItem::getDeleted, true)
                .set(TodoItem::getUpdateTime, LocalDateTime.now())
                .eq(TodoItem::getDeleted, false);
        return todoItemMapper.update(null, wrapper);
    }

    private Integer currentUserId() {
        return UserContextHolder.isLoggedIn();
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Long> normalized = ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (normalized.size() > MAX_BATCH_IDS) {
            throw new BusinessException("单次最多操作" + MAX_BATCH_IDS + "条待办");
        }
        return normalized;
    }

    /**
     * 内容校验：非空、长度上限。非法抛业务异常。
     */
    private String normalizeContent(String content) {
        String c = content == null ? "" : content.trim();
        if (c.isEmpty()) {
            throw new BusinessException("待办内容不能为空");
        }
        if (c.length() > MAX_CONTENT) {
            throw new BusinessException("待办内容不能超过" + MAX_CONTENT + "个字符");
        }
        return c;
    }

    /**
     * 宽松内容处理（导入用）：空白或超长返回 null（调用方跳过）。
     */
    private String contentOrNull(String content) {
        if (content == null) {
            return null;
        }
        String c = content.trim();
        if (c.isEmpty() || c.length() > MAX_CONTENT) {
            return null;
        }
        return c;
    }

    /**
     * 优先级归一化。lenient=true 时非法值降级为 low；否则抛业务异常。
     */
    private String resolvePriority(String priority, boolean lenient) {
        if (StringUtils.isBlank(priority)) {
            return DEFAULT_PRIORITY;
        }
        String low = priority.trim().toLowerCase();
        if (VALID_PRIORITY.contains(low)) {
            return low;
        }
        if (lenient) {
            return DEFAULT_PRIORITY;
        }
        throw new BusinessException("优先级无效：" + priority);
    }

    /**
     * 标签归一化：英文逗号分隔，去重保序，限 15 个、每个最长 30 字符；空则返回 null。
     */
    private String normalizeTags(String tags) {
        if (StringUtils.isBlank(tags)) {
            return null;
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String part : tags.split(",")) {
            String t = part.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (t.length() > MAX_TAG_LEN) {
                t = t.substring(0, MAX_TAG_LEN);
            }
            set.add(t);
            if (set.size() >= MAX_TAGS) {
                break;
            }
        }
        return set.isEmpty() ? null : String.join(",", set);
    }
}
