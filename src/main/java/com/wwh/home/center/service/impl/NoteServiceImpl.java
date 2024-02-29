package com.wwh.home.center.service.impl;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.common.util.FileUtil;
import com.wwh.home.center.model.vo.NoteFileVo;
import com.wwh.home.center.model.vo.NotePathVo;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 笔记服务
 *
 * @author wangwh
 * @date 2024/02/22
 */
@Slf4j
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
    public NoteFileVo getNote(String path) {
        if (StringUtils.isBlank(path)) {
            throw new BusinessException("文件路径不能为空");
        }
        File file = new File(getNoteDir(), path);

        if (!file.exists()) {
            throw new BusinessException("文件【" + path + "】不存在");
        }
        if (file.isDirectory()) {
            throw new BusinessException("【" + path + "】是一个目录");
        }

        try {
            NoteFileVo vo = new NoteFileVo();
            String content = FileUtils.readFileToString(file, SysConstants.DEFAULT_CHARSET);
            vo.setContent(content);
            String name = file.getName();
            vo.setName(name);
            vo.setParentPath(path.substring(0, path.length() - name.length()));

            vo.setFileType(FileUtil.getFileExtension(name));

            FileUtil.FileTimeInfo fti = FileUtil.getFileTimeInfo(file);
            vo.setCreateTime(fti.getCreationTime());
            vo.setUpdateTime(fti.getLastModifiedTime());

            vo.setFavorite(false);
            vo.setAsterisk(true);

            return vo;
        } catch (IOException e) {
            log.error("读取文件异常", e);
            throw new BusinessException("读取文件异常");
        }
    }

    @Override
    public List<NotePathVo> listAll() {
        File baseDir = getNoteDir();
        count = 0;

        long t1 = System.currentTimeMillis();

        System.out.println("开始递归文件....");
        List<NotePathVo> list = recursiveListFile("", baseDir);
        System.out.println("文件递归结束....");
        System.out.println("文件+目录 总共：" + count);
        System.out.println("耗时：" + (System.currentTimeMillis() - t1));
        return list;
    }


    private List<NotePathVo> recursiveListFile(String parentPath, File dir) {
        List<NotePathVo> list = new ArrayList<>();
        File[] filesAndDirs = dir.listFiles();
        for (File fileOrDir : filesAndDirs) {
            NotePathVo pathVo = buildNotePathVo(parentPath, fileOrDir);
            list.add(pathVo);

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
