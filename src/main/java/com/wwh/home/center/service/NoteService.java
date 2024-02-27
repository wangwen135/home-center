package com.wwh.home.center.service;

import com.wwh.home.center.model.vo.NotePathVo;

import java.util.List;

/**
 * 笔记服务
 *
 * @author wangwh
 * @date 2024/02/22
 */
public interface NoteService {

    List<NotePathVo> listAll();

    List<NotePathVo> list(String path);
}
