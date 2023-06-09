package com.csdtb.common.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * @Author zhoujiacheng
 * @Date 2022-11-17
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddExamDTO {

    /**
     * 考核名称
     */
    @NotBlank
    private String name;

    /**
     * 考试开始时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    @NotBlank
    private String startTime;

    /**
     * 考核时长（分钟表示）
     */
    @NotNull
    private Long examDuration;

    /**
     * 监视任务难度（1-简单，2-一般，3-困难）
     */
    @NotNull
    private Integer monitorLevel;

    /**
     * 监视任务时长（分钟）
     */
    @NotNull
    private Integer monitorDuration;

    /**
     * 监视休息时长（分钟）
     */
    @NotNull
    private Integer monitorSleepDuration;

    /**
     * 计算任务难度（1-简单，2-一般，3-困难）
     */
    @NotNull
    private Integer calculateLevel;

    /**
     * 计算固定数量
     */
    private Integer calculateNumber;

    /**
     * 计算固定频率（秒）
     */
    private Integer calculateRate;

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
}
