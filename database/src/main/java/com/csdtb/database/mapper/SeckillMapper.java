package com.csdtb.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csdtb.database.entity.SeckillEntity;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 Dao 接口
 * </p>
 *
 * @author dazhou
 * @since 2023-03-07
 */
public interface SeckillMapper extends BaseMapper<SeckillEntity> {

    void updateByCondition(Long seckillId);
}
