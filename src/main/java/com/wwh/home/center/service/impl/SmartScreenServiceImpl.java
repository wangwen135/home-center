package com.wwh.home.center.service.impl;

import com.wwh.home.center.dao.mapper.PromptMessageMapper;
import com.wwh.home.center.model.entity.FamousQuotes;
import com.wwh.home.center.service.FamousQuotesService;
import com.wwh.home.center.service.SmartScreenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangwh
 * @date 2022/12/27
 */
@Slf4j
@Service
public class SmartScreenServiceImpl implements SmartScreenService {



    @Autowired
    private FamousQuotesService famousQuotesService;

    @Autowired
    private PromptMessageMapper promptMessageMapper;

    @Override
    public String getRandomFamous() {
        FamousQuotes fq = famousQuotesService.getRandomFamousByCache();
        if (fq == null) {
            log.warn("没有有效的名言名句");
            return "";
        }
        return fq.getFamous();
    }


}
