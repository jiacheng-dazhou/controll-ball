package com.csdtb.common.dto.websocket;

import lombok.Data;

import java.util.List;

/**
 * 考核时数据
 */
@Data
public class ExamineData {
    private List<BallData> ballDataList;
    private Integer status;//考试状态 0：监视状态 1：休息状态
    private Long examineTime;//考试剩余时间
    private String arithmeticExpression;//算式
    private String message;//描述
    private Integer isStart;//考试是否开始 0：未开始 1：开始
}
