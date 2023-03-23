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
     * 类型(1-折返,2-出界,3-碰壁,4-计算,5-脑电,6-控制小球)
     */
    private Integer type;

    /**
     * 答案(4-计算)
     */
    private String answer;

    /**
     * id(6-控制小球)
     */
    private Integer id;
    /**
     * 方向(6-控制小球)
     */
    private Integer direction;
    /**
     * 速度(6-控制小球)
     */
    private Integer speed;
}
