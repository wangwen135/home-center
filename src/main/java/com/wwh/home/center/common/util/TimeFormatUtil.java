package com.wwh.home.center.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 时间格式化
 */
public class TimeFormatUtil {

    // 默认时间格式
    private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /**
     * 将 long 类型的时间戳格式化为指定格式的时间字符串
     *
     * @param timestamp 时间戳（毫秒）
     * @param pattern   时间格式
     * @return 格式化后的时间字符串
     */
    public static String format(long timestamp, String pattern) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将 long 类型的时间戳格式化为默认格式的时间字符串
     *
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的时间字符串
     */
    public static String format(long timestamp) {
        return format(timestamp, DEFAULT_DATETIME_PATTERN);
    }

    /**
     * 将 LocalDateTime 类型的时间格式化为指定格式的时间字符串
     *
     * @param dateTime LocalDateTime 对象
     * @param pattern  时间格式
     * @return 格式化后的时间字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将 LocalDateTime 类型的时间格式化为默认的日期时间格式
     *
     * @param dateTime LocalDateTime 对象
     * @return 格式化后的时间字符串
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_DATETIME_PATTERN);
    }

    /**
     * 将 LocalDate 类型的日期格式化为指定格式的日期字符串
     *
     * @param date    LocalDate 对象
     * @param pattern 日期格式
     * @return 格式化后的日期字符串
     */
    public static String format(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将 LocalDate 类型的日期格式化为默认的日期格式
     *
     * @param date LocalDate 对象
     * @return 格式化后的日期字符串
     */
    public static String format(LocalDate date) {
        return format(date, DEFAULT_DATE_PATTERN);
    }

    /**
     * 将 LocalTime 类型的时间格式化为指定格式的时间字符串
     *
     * @param time    LocalTime 对象
     * @param pattern 时间格式
     * @return 格式化后的时间字符串
     */
    public static String format(LocalTime time, String pattern) {
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将 LocalTime 类型的时间格式化为默认的时间格式
     *
     * @param time LocalTime 对象
     * @return 格式化后的时间字符串
     */
    public static String format(LocalTime time) {
        return format(time, DEFAULT_TIME_PATTERN);
    }

    public static void main(String[] args) {
        long currentTimestamp = System.currentTimeMillis();
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // long 类型的时间戳
        System.out.println("默认格式化 long 类型: " + format(currentTimestamp));
        System.out.println("自定义格式化 long 类型: " + format(currentTimestamp, "yyyy/MM/dd HH:mm"));

        // LocalDateTime 类型
        System.out.println("默认格式化 LocalDateTime 类型: " + format(nowDateTime));
        System.out.println("自定义格式化 LocalDateTime 类型: " + format(nowDateTime, "yyyy/MM/dd HH:mm:ss"));

        // LocalDate 类型
        System.out.println("默认格式化 LocalDate 类型: " + format(today));
        System.out.println("自定义格式化 LocalDate 类型: " + format(today, "dd/MM/yyyy"));

        // LocalTime 类型
        System.out.println("默认格式化 LocalTime 类型: " + format(currentTime));
        System.out.println("自定义格式化 LocalTime 类型: " + format(currentTime, "HH:mm"));
    }
}

