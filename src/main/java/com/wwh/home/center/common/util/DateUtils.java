package com.wwh.home.center.common.util;

import com.iceyyy.nongli.NongLi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 日历工具
 *
 * @author wangwh
 * @date 2022/12/27
 */
public class DateUtils {

    public static final Map<Integer, String> weekMap = new HashMap<Integer, String>() {
        {
            put(1, "星期一");
            put(2, "星期二");
            put(3, "星期三");
            put(4, "星期四");
            put(5, "星期五");
            put(6, "星期六");
            put(7, "星期日");
        }
    };
    /**
     * yyyy-MM-dd
     */
    public static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * HH:mm:ss
     */
    public static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter FORMATTER_DEFAULT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * yyyyMMddHHmmss
     */
    public static final DateTimeFormatter FORMATTER_NUMBER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    /**
     * 获取当前时间的格式化字符串
     *
     * @param formatter
     * @return
     */
    public static String getCurrentDateTimeFormat(DateTimeFormatter formatter) {
        if (formatter == null) {
            formatter = FORMATTER_DEFAULT;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }


    /**
     * 获取当天的农历
     *
     * @return 如：二零二二年冬月初八
     */
    public static String getNongLi() {
        LocalDate localDate = LocalDate.now();
        return NongLi.getDate(localDate.toString());
    }

    /**
     * 获取当天农历
     *
     * @return 如：冬月初八
     */
    public static String getNongLiShort() {
        String str = getNongLi();
        return str.substring(str.indexOf("年") + 1);
    }

    /**
     * 获取当天是星期几
     *
     * @return
     */
    public static String getWeek() {
        LocalDate localDate = LocalDate.now();
        return weekMap.get(localDate.getDayOfWeek().getValue());
    }

    /**
     * 获取月份和日期
     *
     * @return 如：12月27日
     */
    public static String getMonthAndDay() {
        LocalDate localDate = LocalDate.now();
        return localDate.getMonthValue() + "月" + localDate.getDayOfMonth() + "日";
    }

    /**
     * 获取日期和星期
     *
     * @return 如：12月27日 星期二
     */
    public static String getDateAndWeek() {
        LocalDate localDate = LocalDate.now();
        return localDate.getMonthValue() + "月" + localDate.getDayOfMonth() + "日 " + weekMap.get(localDate.getDayOfWeek().getValue());
    }

    public static void main(String[] args) {
        System.out.println(getMonthAndDay());
        System.out.println(getDateAndWeek());
    }

    public static void main2(String[] args) {
        LocalDate localDate = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            print(localDate.minusDays(i));
        }
    }

    private static void print(LocalDate localDate) {
        System.out.println("+++++++++++++++++++++++++++++++++++");
        System.out.println(localDate.toString());
        System.out.println(localDate.getMonthValue());
        System.out.println(localDate.getDayOfMonth());
        System.out.println(localDate.getDayOfWeek());
        System.out.println(localDate.getDayOfWeek().getValue());
        System.out.println(weekMap.get(localDate.getDayOfWeek().getValue()));

        String str = NongLi.getDate(localDate.toString());
        //String str = NongLi.getDate("2022-11-22");
        System.out.println(str);
        System.out.println(str.substring(str.indexOf("年") + 1));
    }
}
