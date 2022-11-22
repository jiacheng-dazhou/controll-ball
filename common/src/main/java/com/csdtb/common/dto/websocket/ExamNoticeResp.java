package com.csdtb.common.dto.websocket;

import lombok.Data;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-18
 * @Description 考试交互消息通知
 **/
@Data
public class ExamNoticeResp {

    /**
     * 类型(1-折返,2-出界,3-碰壁,4-计算,5-脑电测试)
     */
    private Integer type;

    /**
     * 名称
     */
    private String name;

    /**
     * 出现时间
     */
    private String startTime;

    /**
     * 正确与否
     */
    private Boolean isCorrect;

    /**
     * 是否提前反应(1-折返,2-出界,3-碰壁)
     */
    private Boolean isReactInAdvance;

    /**
     * 判断反应时间(秒）
     */
    private String reactionTime;

    /**
     * 判断效率
     */
    private String judgmentEfficiency;

    /**
     * 答案(4-计算)
     */
    private String answer;
}
