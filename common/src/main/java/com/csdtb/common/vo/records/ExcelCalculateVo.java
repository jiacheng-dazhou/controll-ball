package com.csdtb.common.vo.records;

import lombok.Data;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-23
 **/
@Data
public class ExcelCalculateVo {
    /**
     * 序号
     */
    private Integer id;
    /**
     * 名称
     */
    private String name;
    /**
     * 答案
     */
    private String answer;
    /**
     * 出现时间(格式：HH:mm:ss)
     */
    private String startTime;
    /**
     * 是否正确(0-错误，1-正确)
     */
    private String isCorrect;
    /**
     * 判断反应时间(秒）
     */
    private String reactionTime;
    /**
     * 判断效率
     */
    private String judgmentEfficiency;
}
