package com.wwh.home.center.controller;

import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.vo.NotePathVo;
import com.wwh.home.center.service.NoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 笔记
 *
 * @author wangwh
 * @date 2024/02/22
 */
@Slf4j
@Validated
@RestController
@Api(tags = "笔记")
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @ApiOperation("获取路径信息")
    @GetMapping("/list")
    public Result<List<NotePathVo>> list(@RequestParam(required = false) @ApiParam("子路径") String path) {
        return Result.success(noteService.list(path));
    }

    //创建目录

    //创建文件

    //保存文件


}
