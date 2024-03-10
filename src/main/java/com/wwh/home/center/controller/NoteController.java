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

import javax.validation.Valid;
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

    @ApiOperation("创建目录")
    @PostMapping("/createDir")
    public Result<NotePathVo> createDir(@RequestParam @ApiParam(value = "路径", required = true) String path,
                                        @RequestParam(required = false) @ApiParam("目录名称") String name) {
        return Result.success(noteService.createDir(path, name));
    }

    @ApiOperation("创建文件")
    @PostMapping("/createFile")
    public Result<NotePathVo> createFile(@RequestParam @ApiParam(value = "路径", required = true) String path,
                                         @RequestParam(required = false) @ApiParam("文件名称") String name) {
        return Result.success(noteService.createFile(path, name));
    }

    //修改目录名称

    //创建文件

    @ApiOperation("修改文件名")
    @PostMapping("/rename")
    public Result rename(@RequestParam @ApiParam(value = "文件全路径", required = true) String filePath,
                         @RequestParam @ApiParam(value = "新的文件名称", required = true) String newName) {
        noteService.reName(filePath, newName);
        return Result.success();
    }

    @ApiOperation("保存文件")
    @PostMapping("/save")
    public Result<NoteFileVo> saveNote(@Valid @RequestBody @ApiParam NoteFileVo fileVo) {
        return Result.success(noteService.saveNote(fileVo));
    }

}
