package com.wwh.home.center.service.impl;

import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.common.util.FileUtil;
import com.wwh.home.center.model.vo.NotePathVo;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.NoteService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 笔记服务
 *
 * @author wangwh
 * @date 2024/02/22
 */
@Service
public class NoteServiceImpl implements NoteService {

    @Value("${notes.base-path}")
    private String noteBasePath;

    private File getNoteDir() {
        UserContextHolder.isLoggedIn();
        String username = UserContextHolder.getUsername();
        File noteDir = new File(noteBasePath, username);
        if (!noteDir.exists()) {
            noteDir.mkdirs();
        }
        return noteDir;
    }


    @Override
    public List<NotePathVo> list(String path) {
        File dir = getNoteDir();
        if (StringUtils.isNotBlank(path)) {
            dir = new File(dir, path);
        } else {
            path = "";
        }
        if (!dir.exists()) {
            throw new BusinessException("目录【" + path + "】不存在");
        }
        if (!dir.isDirectory()) {
            throw new BusinessException("【" + path + "】不是一个目录");
        }
        List<NotePathVo> list = new ArrayList<>();

        File[] filesAndDirs = dir.listFiles();
        for (File fileOrDir : filesAndDirs) {

            NotePathVo vo = new NotePathVo();
            String fileName = fileOrDir.getName();
            vo.setName(fileName);
            vo.setParentPath(path);
            list.add(vo);
            if (fileOrDir.isFile()) {
                vo.setDir(false);
                vo.setFileType(FileUtil.getFileExtension(fileName));

            } else if (fileOrDir.isDirectory()) {
                vo.setDir(true);
                vo.setFileType("DIR");
            }
        }

        return list;
    }
}
