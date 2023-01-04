package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwh.home.center.dao.mapper.FamousQuotesMapper;
import com.wwh.home.center.model.entity.FamousQuotes;
import com.wwh.home.center.service.FamousQuotesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * @author wangwh
 * @date 2022/12/27
 */
@Slf4j
@Service
public class FamousQuotesServiceImpl implements FamousQuotesService {
    @Autowired
    private FamousQuotesMapper famousQuotesMapper;

    @Override
    public FamousQuotes getRandomFamous() {
        List<FamousQuotes> list = getAllFamous();
        if (list == null || list.isEmpty()) {
            return null;
        }

        log.debug("按照权重随机取一条名言");

        int sumWeight = list.stream().mapToInt(FamousQuotes::getWeight).sum();
        int randomInt = new Random().nextInt(sumWeight);

        int accumulated = 0;
        for (FamousQuotes fq : list) {
            accumulated += fq.getWeight();
            if (accumulated >= randomInt) {
                return fq;
            }
        }
        return list.get(0);
    }

    @Override
    public List<FamousQuotes> getAllFamous() {
        QueryWrapper<FamousQuotes> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FamousQuotes::getDeleted, false);
        return famousQuotesMapper.selectList(queryWrapper);
    }

    @Override
    @Cacheable("getRandomFamousByCache")
    public FamousQuotes getRandomFamousByCache() {
        return getRandomFamous();
    }

    @Override
    public FamousQuotes getFixedDisplayFamous() {
        QueryWrapper<FamousQuotes> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FamousQuotes::getDeleted, false).eq(FamousQuotes::getFixedDisplay, true)
                .orderByDesc(FamousQuotes::getWeight).orderByDesc(FamousQuotes::getId)
                .last("limit 1");
        return famousQuotesMapper.selectOne(queryWrapper);
    }
}
