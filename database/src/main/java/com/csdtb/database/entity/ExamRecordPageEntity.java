package com.csdtb.database.entity;

import lombok.Data;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-21
 **/
@Data
public class ExamRecordPageEntity{
    /**
     * 考核记录 主键id
     */
    private Integer id;
    /**
     * 考核名称
     */
    private String name;
    /**
     * 考试开始时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    private String startTime;
    /**
     * 考试状态(1-未开考，2-即将开始，3-进行中，4-已完成）
     */
    private Integer status;
    /**
     * 用户姓名
     */
    private String username;
}
