package com.wwh.home.center.common;

import com.iceyyy.nongli.NongLi;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author wangwh
 * @date 2022/12/27
 */
public class NongliTest {

    public static void main2(String[] args) {
        String date = "20221227";
        String str = NongLi.getDate(date);
        System.out.println(str);
    }


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

    public static void main(String[] args) {
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
