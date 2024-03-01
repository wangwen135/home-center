package com.wwh.home.center.controller;

import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.vo.NoteFileVo;
import com.wwh.home.center.model.vo.NotePathVo;
import com.wwh.home.center.service.NoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("获取全部的笔记列表")
    @GetMapping("/listAll")
    public Result<List<NotePathVo>> listAll() {
        return Result.success(noteService.listAll());
    }

    @ApiOperation("获取指定路径下的笔记列表")
    @GetMapping("/list")
    public Result<List<NotePathVo>> list(@RequestParam(required = false) @ApiParam("子路径") String path) {
        return Result.success(noteService.list(path));
    }


    @ApiOperation("获取笔记文件")
    @GetMapping("/getNote")
    public Result<NoteFileVo> getNote(@RequestParam @ApiParam(value = "笔记路径", required = true) String path) {
        return Result.success(noteService.getNote(path));
    }

    //创建目录

    //创建文件

    //保存文件
    @ApiOperation("保存文件")
    @PostMapping("/save")
    public Result<NoteFileVo> saveNote(@RequestBody @ApiParam NoteFileVo fileVo) {
        return Result.success(noteService.saveNote(fileVo));
    }

}
