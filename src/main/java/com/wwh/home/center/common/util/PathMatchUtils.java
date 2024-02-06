package com.wwh.home.center.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * 路径匹配工具类
 * <pre>
 * ?：匹配一个字符
 * *：匹配零个或多个字符，但不包括路径分隔符（/）
 * **：匹配零个或多个目录或文件
 * </pre>
 *
 * @author wangwh
 * @date 2024/02/06
 */
@Slf4j
public class PathMatchUtils {

    private static AntPathMatcher antPathMatcher = new AntPathMatcher();

    public static boolean matchList(String path, List<String> matcherList) {
        if (matcherList == null || matcherList.isEmpty()) {
            return false;
        }
        for (String pattern : matcherList) {
            if (StringUtils.isBlank(pattern)) {
                continue;
            }
            if (antPathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
