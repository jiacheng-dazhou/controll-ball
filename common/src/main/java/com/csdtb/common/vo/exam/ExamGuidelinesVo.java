package com.csdtb.common.vo.exam;

import lombok.Data;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-17
 **/
@Data
public class ExamGuidelinesVo {
    /**
     * 指导语内容
     */
    private String guidingWords;

    /**
     * 指导语时长（1-30秒，2-1分钟，3-2分钟，4-3分钟）
     */
    private Integer guidingWordsDuration;

    /**
     * 模拟考核时长（1-1分钟，2-2分钟，3-3分钟）
     */
    private Integer simulateExamDuration;

    /**
     * 模拟小球数量（1，2，3，4，5）
     */
    private Integer simulateBallNumber;

    /**
     * 模拟小球速度(1-慢、2-中、3-快)
     */
    private Integer simulateBallSpeed;

    /**
     * 模拟考核计算数量（1，2，3，4，5）
     */
    private Integer simulateCalculateNumber;

    /**
     * 模拟考核计算难度(1-简单，2-一般，3-困难)
     */
    private Integer simulateCalculateLevel;
}
