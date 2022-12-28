package com.wwh.home.center.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 提示信息
 *
 * @author wangwh
 * @date 2022/12/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptMessageVo {
    private Integer id;
    private String message;
    private Boolean showOnly;
}
