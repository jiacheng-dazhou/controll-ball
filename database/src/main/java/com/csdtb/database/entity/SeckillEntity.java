package com.csdtb.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系
 * </p>
 *
 * @author dazhou
 * @since 2023-03-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_seckill")
@Accessors(chain = true)
public class SeckillEntity {

    /**
     * 优惠券id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 库存
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 生效时间
     */
    @TableField("begin_time")
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


}
