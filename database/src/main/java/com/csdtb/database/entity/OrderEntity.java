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
 * 订单表
 * </p>
 *
 * @author dazhou
 * @since 2023-03-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_order")
@Accessors(chain = true)
public class OrderEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 下单的用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 秒杀商品id
     */
    @TableField("seckill_id")
    private Long seckillId;

    /**
     * 下单时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


}
