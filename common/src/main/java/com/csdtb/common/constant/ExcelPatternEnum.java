package com.csdtb.common.constant;

import lombok.Getter;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-23
 **/
@Getter
public enum ExcelPatternEnum {
    USER_BASE_INFO("用户基础信息","姓名:%s年龄:%s性别:%s管制单位:%s职称:%s职务:%s","考核时间:%s考核总时长:%s考核时长:%s休息次数:%s休息总时长:%s"),
    MONITOR_INFO("监控任务","监控任务    任务时长:%s难度:%s出界总数:%s碰撞总数:%s折返总数:%s","序号,名称,出现时间,按钮正确与否,是否提前反应,判断反应时间,判断效率","出界正确率:%s折返正确率:%s碰撞正确率:%s"),
    CALCULATE_INFO("计算任务","计算任务    题目数量:%s难度:%s","序号,题目,答案,出现时间,正确与否,反应时间,反应效率","正确率:%s");
    private String description;
    private String[] pattern;

    ExcelPatternEnum(String description,String... pattern) {
        this.pattern = pattern;
        this.description = description;
    }
}
