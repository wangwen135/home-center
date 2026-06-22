package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片上传记录
 *
 * <p>数据库只保存图片访问路径与必要元数据，不保存图片二进制。
 * 用于上传审计、文件清理与库存查询。</p>
 */
@Data
@TableName("image_record")
public class ImageRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 相对访问路径，例如 {@code 2024/01/xxxx.png}
     */
    private String relativePath;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime uploadTime;

    /**
     * 上传用户 ID（未登录场景为空）
     */
    private Integer uploadUserId;
}
