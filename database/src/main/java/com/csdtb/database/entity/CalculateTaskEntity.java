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
 * 计算任务表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_calculate_task")
public class CalculateTaskEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 难度(1-简单，2-一般，3-困难)
     */
    @TableField("level")
    private Integer level;

    /**
     * 难度1：一位数加减法（10%-90%）
     */
    @TableField("calculate_level_1")
    private Integer calculateLevel1;

    /**
     * 难度2：两位数加减法（不进位不退位，答案是两位数）（10%-90%）
     */
    @TableField("calculate_level_2")
    private Integer calculateLevel2;

    /**
     * 难度3：两位数加减法（进位退位，答案是两位数）（10%-90%）
     */
    @TableField("calculate_level_3")
    private Integer calculateLevel3;

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
