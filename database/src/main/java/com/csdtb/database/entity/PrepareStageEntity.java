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
 * 准备阶段表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_prepare_stage")
public class PrepareStageEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 指导语内容
     */
    @TableField("guiding_words")
    private String guidingWords;

    /**
     * 指导语时长（1-30秒，2-1分钟，3-2分钟，4-3分钟）
     */
    @TableField("guiding_words_duration")
    private Integer guidingWordsDuration;

    /**
     * 模拟考核时长（1-1分钟，2-2分钟，3-3分钟）
     */
    @TableField("simulate_exam_duration")
    private Integer simulateExamDuration;

    /**
     * 模拟小球数量（1，2，3，4，5）
     */
    @TableField("simulate_ball_number")
    private Integer simulateBallNumber;

    /**
     * 模拟小球速度(1-慢、2-中、3-快)
     */
    @TableField("simulate_ball_speed")
    private Integer simulateBallSpeed;

    /**
     * 模拟考核计算数量（1，2，3，4，5）
     */
    @TableField("simulate_calculate_number")
    private Integer simulateCalculateNumber;

    /**
     * 模拟考核计算难度(1-简单，2-一般，3-困难)
     */
    @TableField("simulate_calculate_level")
    private Integer simulateCalculateLevel;

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
