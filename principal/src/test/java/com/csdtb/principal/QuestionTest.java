package com.csdtb.principal;

import java.util.Set;
import java.util.TreeSet;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-24
 **/
public class QuestionTest {
    public static void main(String[] args) {
        Set<String> set = new TreeSet<>();

        //简单难度题库
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                if (i + j < 10) {
                    set.add(String.format("%s+%s",i,j));
                }
                if (i - j > 0) {
                    set.add(String.format("%s-%s",i,j));
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        set.forEach(item-> builder.append(item).append(","));
        System.out.println(builder);

        //一般难度题库
        set.clear();
        for (int i = 10; i < 100; i++) {
            for (int j = 10; j < 100; j++) {
                String c = String.valueOf(i).substring(1);
                String d = String.valueOf(j).substring(1);
                if (i + j < 100 && Integer.valueOf(c) + Integer.valueOf(d) < 10) {
                    set.add(String.format("%s+%s",i,j));
                }
                if (i - j > 10 && Integer.valueOf(c) - Integer.valueOf(d) > 0) {
                    set.add(String.format("%s-%s",i,j));
                }
            }
        }
        StringBuilder builder1 = new StringBuilder();
        set.forEach(item-> builder1.append(item).append(","));
        System.out.println(builder1);

        //困难难度题库
        set.clear();
        for (int i = 10; i < 100; i++) {
            for (int j = 10; j < 100; j++) {
                String c = String.valueOf(i).substring(1);
                String d = String.valueOf(j).substring(1);
                if (i + j < 100 && Integer.valueOf(c) + Integer.valueOf(d) > 10) {
                    set.add(String.format("%s+%s",i,j));
                }
                if (i - j > 10 && Integer.valueOf(c) - Integer.valueOf(d) < 0) {
                    set.add(String.format("%s-%s",i,j));
                }
            }
        }
        StringBuilder builder2 = new StringBuilder();
        set.forEach(item-> builder2.append(item).append(","));
        System.out.println(builder2);
    }
}
