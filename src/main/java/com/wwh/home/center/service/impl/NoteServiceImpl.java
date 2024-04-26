package com.wwh.home.center.service.impl;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.common.util.DateUtils;
import com.wwh.home.center.common.util.FileUtil;
import com.wwh.home.center.model.vo.NoteFileVo;
import com.wwh.home.center.model.vo.NotePathVo;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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

    @Value("${notes.recycle-base-path}")
    private String noteRecycleBasePath;

    private File getUserDirectory(String basePath) {
        UserContextHolder.isLoggedIn();
        String username = UserContextHolder.getUsername();
        File userDir = new File(basePath, username);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        return userDir;
    }

    private File getNoteDir() {
        return getUserDirectory(noteBasePath);
    }

    private File getNoteRecycleDir() {
        return getUserDirectory(noteRecycleBasePath);
    }

    @Override
    public NoteFileVo getNote(String path) {
        File file = getFileByPath(path);

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

    private File getFileByPath(String path) {
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
        return file;
    }

    private File getDirByPath(String path) {
        if (StringUtils.isBlank(path)) {
            throw new BusinessException("文件夹路径不能为空");
        }
        File file = new File(getNoteDir(), path);

        if (!file.exists()) {
            throw new BusinessException("目录【" + path + "】不存在");
        }
        if (!file.isDirectory()) {
            throw new BusinessException("【" + path + "】不是一个目录");
        }
        return file;
    }

    @Override
    public NoteFileVo saveNote(NoteFileVo fileVo) {
        log.info("保存文档：{}{}", fileVo.getParentPath(), fileVo.getName());

        String name = fileVo.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("文件名称不能为空");
        }
        try {
            File dir = getNoteDir();
            String path = fileVo.getParentPath();
            if (StringUtils.isNotBlank(path)) {
                dir = new File(dir, path);
                dir.mkdirs();
            }
            File file = new File(dir, name);

            FileUtils.write(file, fileVo.getContent(), SysConstants.DEFAULT_CHARSET);

            FileUtil.FileTimeInfo fti = FileUtil.getFileTimeInfo(file);
            fileVo.setCreateTime(fti.getCreationTime());
            fileVo.setUpdateTime(fti.getLastModifiedTime());
            return fileVo;

        } catch (IOException e) {
            log.error("写入文件异常", e);
            throw new BusinessException("保存文件异常");
        }
    }

    @Override
    public NotePathVo createDir(String path, String name) {

        File pathDir = getPathDir(path);

        if (StringUtils.isBlank(name)) {
            name = "新建文件夹";
        }
        name = name.trim();

        name = FileUtil.calcValidDirName(pathDir, name);

        File createDir = new File(pathDir, name);
        createDir.mkdir();

        return buildNotePathVo(path, createDir);
    }

    private File getPathDir(String path) {
        if (StringUtils.isBlank(path)) {
            throw new BusinessException("路径不能为空");
        }
        File pathDir = new File(getNoteDir(), path);

        if (!pathDir.exists()) {
            //创建目录
            pathDir.mkdirs();
        } else if (pathDir.isFile()) {
            throw new BusinessException(path + " 是一个文件");
        }
        return pathDir;
    }

    @Override
    public NotePathVo createFile(String path, String name) {
        File pathDir = getPathDir(path);

        if (StringUtils.isBlank(name)) {
            name = "新建文件.md";
        }
        name = name.trim();

        name = FileUtil.calcValidFileName(pathDir, name);

        File createFile = new File(pathDir, name);

        try {
            createFile.createNewFile();
        } catch (IOException e) {
            log.error("创建文件异常", e);
            throw new BusinessException("创建文件异常");
        }
        return buildNotePathVo(path, createFile);
    }

    @Override
    public boolean reName(String filePath, String newName) {
        File file = getFileByPath(filePath);
        //判断是否存在重名的文件
        File newFile = new File(file.getParentFile(), newName);
        if (newFile.exists()) {
            //TODO 这个需要定义特定的错误代码
            throw new BusinessException("文件名重复");
        }
        try {
            return file.renameTo(newFile);
        } catch (Exception e) {
            log.error("将文件：{} 重命名为：{} 异常", filePath, newName, e);
            throw new BusinessException("重命名文件失败！");
        }
    }

    @Override
    public boolean deleteDir(String path) {
        return false;
    }

    @Override
    public boolean deleteFile(String filePath) {
        File file = getFileByPath(filePath);
        String timeNumber = DateUtils.getCurrentDateTimeFormat(DateUtils.FORMATTER_NUMBER);
        File dest = new File(getNoteRecycleDir(), filePath + "." + timeNumber);
        try {
            FileUtils.moveFile(file, dest);
        } catch (IOException e) {
            log.error("移动文件异常", e);
            return false;
        }
        return true;
    }

    private int count = 0;

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
