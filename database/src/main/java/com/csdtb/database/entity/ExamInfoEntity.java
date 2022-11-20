package com.csdtb.database.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 考试信息表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_exam_info")
public class ExamInfoEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考核名称
     */
    @TableField("name")
    private String name;

    /**
     * 考试开始时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    @TableField("start_time")
    private String startTime;

    /**
     * 考试结束时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    @TableField("end_time")
    private String endTime;

    /**
     * 监视任务难度（1-简单，2-一般，3-困难）
     */
    @TableField("monitor_level")
    private Integer monitorLevel;

    /**
     * 监视任务时长（分钟）
     */
    @TableField("monitor_duration")
    private Integer monitorDuration;

    /**
     * 监视休息时长（分钟）
     */
    @TableField("monitor_sleep_duration")
    private Integer monitorSleepDuration;

    /**
     * 计算任务难度（1-简单，2-一般，3-困难）
     */
    @TableField("calculate_level")
    private Integer calculateLevel;

    /**
     * 计算固定数量
     */
    @TableField("calculate_number")
    private Integer calculateNumber;

    /**
     * 计算固定频率（秒）
     */
    @TableField("calculate_rate")
    private Integer calculateRate;

    /**
     * 脑电测试开始时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    @TableField("erp_start_time")
    private String erpStartTime;

    /**
     * 脑电测试结束时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    @TableField("erp_end_time")
    private String erpEndTime;

    /**
     * 考核说明
     */
    @TableField("description")
    private String description;

    /**
     * 考试状态(1-未开考，2-即将开始，3-进行中，4-已完成）
     */
    @TableField("status")
    private Integer status;

    /**
     * 考试题库
     */
    @TableField("questions")
    private String questions;

    /**
     * 删除标志:1:删除，0：未删除
     */
    @TableField("is_delete")
    private Boolean isDelete;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

}
