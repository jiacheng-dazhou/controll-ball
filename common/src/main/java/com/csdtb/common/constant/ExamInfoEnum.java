package com.csdtb.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-17
 **/
@Getter
@AllArgsConstructor
public enum ExamInfoEnum {
    //考核状态
    NO_EXAMINATION(1,"未开考"),
    BEGIN_IN_A_MINUTE(2,"即将开始"),
    IN_PROGRESS(3,"进行中"),
    COMPLETED(4,"已完成"),

    TURNBACK(1,"折返"),
    BOUNDS(2,"出界"),
    CRASH(3,"碰撞"),
    CALCULATE(4,"计算"),
    CONTROL_BALL(6,"控制小球");

    private Integer status;
    private String description;
}
