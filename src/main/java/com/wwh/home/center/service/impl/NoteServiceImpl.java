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

    private int count = 0;

    @Override
    public List<NotePathVo> listAll() {
        File baseDir = getNoteDir();
        count = 0;
        System.out.println("开始递归文件....");
        List<NotePathVo> list = recursiveListFile("/", baseDir);
        System.out.println("文件递归结束....");
        System.out.println("文件+目录 总共：" + count);
        return list;
    }


    private List<NotePathVo> recursiveListFile(String parentPath, File dir) {
        List<NotePathVo> list = new ArrayList<>();
        File[] filesAndDirs = dir.listFiles();
        for (File fileOrDir : filesAndDirs) {
            NotePathVo pathVo = buildNotePathVo(parentPath, fileOrDir);
            list.add(pathVo);

            System.out.print("v");
            count++;

            if (fileOrDir.isDirectory()) {
                pathVo.setChildren(recursiveListFile(pathVo.getFullPath(), fileOrDir));
            }
        }
        return list;
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
            list.add(buildNotePathVo(path, fileOrDir));
        }
        return list;
    }

    private NotePathVo buildNotePathVo(String parentPath, File fileOrDir) {
        NotePathVo vo = new NotePathVo();
        String fileName = fileOrDir.getName();
        vo.setName(fileName);
        vo.setParentPath(parentPath);
        if (fileOrDir.isFile()) {
            vo.setDir(false);
            vo.setFileType(FileUtil.getFileExtension(fileName));
        } else if (fileOrDir.isDirectory()) {
            vo.setDir(true);
            vo.setFileType("DIR");
        }
        return vo;
    }
}
