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
    COMPLETED(4,"已完成");

    private Integer status;
    private String description;
}
