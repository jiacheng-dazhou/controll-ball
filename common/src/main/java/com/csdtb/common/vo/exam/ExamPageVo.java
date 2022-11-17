package com.csdtb.common.vo.exam;

import lombok.Data;


/**
 * @Author zhoujiacheng
 * @Date 2022-11-17
 **/
@Data
public class ExamPageVo {
    /**
     * 主键id
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
     * 考试结束时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    private String endTime;

    /**
     * 监视任务难度（1-简单，2-一般，3-困难）
     */
    private Integer monitorLevel;

    /**
     * 计算任务难度（1-简单，2-一般，3-困难）
     */
    private Integer calculateLevel;


    /**
     * 脑电测试开始时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    private String erpStartTime;

    /**
     * 脑电测试结束时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    private String erpEndTime;

    /**
     * 考核说明
     */
    private String description;

    /**
     * 考试状态(1-未开考，2-即将开始，3-进行中，4-已完成）
     */
    private Integer status;
}
