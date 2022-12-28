package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwh.home.center.dao.mapper.PromptMessageMapper;
import com.wwh.home.center.model.entity.PromptMessage;
import com.wwh.home.center.service.PromptMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author wangwh
 * @date 2022/12/28
 */
@Slf4j
@Service
public class PromptMessageServiceImpl implements PromptMessageService {

    @Autowired
    private PromptMessageMapper promptMessageMapper;

    @Override
    public PromptMessage getTheBestPromptMessage() {
        QueryWrapper<PromptMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false).gt("expiration_time", new Date())
                .orderByDesc("weight").last("limit 1");
        return promptMessageMapper.selectOne(queryWrapper);
    }
}
