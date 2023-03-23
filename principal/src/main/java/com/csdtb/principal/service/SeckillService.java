package com.csdtb.principal.service;

import com.csdtb.common.ResponseResult;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务类
 * </p>
 *
 * @author dazhou
 * @since 2023-03-07
 */
public interface SeckillService {

    ResponseResult seckillWithRedisLock(Long id);

    void deal(Long userId, Long seckillId);
}
