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
 * 小球监控任务表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_ball_monitor_task")
public class BallMonitorTaskEntity {

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
     * 小球数量（1-10）
     */
    @TableField("number")
    private Integer number;

    /**
     * 小球速度(1-慢、2-中、3-快)
     */
    @TableField("speed")
    private Integer speed;

    /**
     * 折返小球颜色(1-蓝，2-绿)
     */
    @TableField("turnback_color")
    private Integer turnbackColor;

    /**
     * 小球折返率（10%-90%）
     */
    @TableField("turnback_rate")
    private Integer turnbackRate;

    /**
     * 出界小球颜色(1-蓝，2-绿)
     */
    @TableField("bounds_color")
    private Integer boundsColor;

    /**
     * 小球出界率（10%-90%）
     */
    @TableField("bounds_rate")
    private Integer boundsRate;

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
