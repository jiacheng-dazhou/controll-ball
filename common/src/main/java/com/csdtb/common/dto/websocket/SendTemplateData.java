package com.csdtb.common.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalTime;
import java.util.List;

/**
 * @Author zhoujiacheng
 * @Date 2022-12-14
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SendTemplateData {
    /**
     * 小球信息
     */
    private List<Ball> balls;
    /**
     * 小球折返颜色
     */
    private Integer turnbackColor;
    /**
     * 小球出界颜色
     */
    private Integer boundsColor;
    /**
     * 监视状态(0-监视，1-休息)
     */
    private Integer status = 0;
    /**
     * 当前题目
     */
    private String question;
    /**
     * 考试倒计时
     */
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime examCountDown;
}
