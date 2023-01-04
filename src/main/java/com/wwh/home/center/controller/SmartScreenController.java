package com.wwh.home.center.controller;

import com.wwh.home.center.model.entity.PromptMessage;
import com.wwh.home.center.model.vo.PromptMessageVo;
import com.wwh.home.center.service.PromptMessageService;
import com.wwh.home.center.service.SmartScreenService;
import com.wwh.home.center.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能屏
 *
 * @author wangwh
 * @date 2022/12/27
 */
@Slf4j
@RestController
@RequestMapping("/smartScreen")
public class SmartScreenController {

    @Autowired
    private SmartScreenService smartScreenService;

    @Autowired
    private PromptMessageService promptMessageService;

    @GetMapping("/test")
    public String test() {
        return "this is test msg";
    }

    @GetMapping("/getRandomFamous")
    public String getRandomFamous() {
        return smartScreenService.getRandomFamous();
    }

    @GetMapping("/getNongLi")
    public String getNongLi() {
        return DateUtils.getNongLi();
    }

    @GetMapping("/getNongLiShort")
    public String getNongLiShort() {
        return DateUtils.getNongLiShort();
    }

    @GetMapping("/getDateAndWeek")
    public String getDateAndWeek() {
        return DateUtils.getDateAndWeek();
    }



    @GetMapping("/getPromptMessage")
    public PromptMessageVo getPromptMessage() {
        PromptMessage pm = promptMessageService.getTheBestPromptMessage();
        if (pm != null) {
            PromptMessageVo vo = new PromptMessageVo();
            BeanUtils.copyProperties(pm, vo);
            return vo;
        }
        return null;
    }
}
