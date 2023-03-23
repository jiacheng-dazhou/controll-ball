package com.csdtb.principal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csdtb.common.LoginUserHolder;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.dto.user.UserDTO;
import com.csdtb.database.entity.OrderEntity;
import com.csdtb.database.entity.SeckillEntity;
import com.csdtb.database.mapper.OrderMapper;
import com.csdtb.database.mapper.SeckillMapper;
import com.csdtb.principal.service.SeckillService;
import com.csdtb.principal.util.RedisUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author dazhou
 * @since 2023-03-07
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private SeckillMapper seckillMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ApplicationContext applicationContext;

    private static final String lockPrefix = UUID.randomUUID().toString().replace("-", "");

    @Override
    public ResponseResult seckillWithRedisLock(Long id) {
        SeckillEntity seckillEntity = seckillMapper.selectById(id);
        if (seckillEntity == null) {
            return ResponseResult.error("当前秒杀商品不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillEntity.getBeginTime())) {
            return ResponseResult.error("还未开始秒杀");
        }
        if (now.isAfter(seckillEntity.getEndTime())) {
            return ResponseResult.error("商品秒杀已结束");
        }
        if (seckillEntity.getStock() <= 0) {
            return ResponseResult.error("商品余量不足");
        }
        UserDTO user = LoginUserHolder.getUser();
        OrderEntity orderEntity = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getSeckillId, id)
                .eq(OrderEntity::getUserId, user.getId()));
        if (orderEntity != null) {
            return ResponseResult.error("请勿重复秒杀");
        }
        String lockKey = "seckill:" + user.getId();
        String lockValue = lockPrefix + Thread.currentThread().getId();
        RedisUtil redisUtil = new RedisUtil(redisTemplate);
        if (!redisUtil.tryLock(lockKey, lockValue)) {
            return ResponseResult.error("不允许重复下单");
        }
        //下单
        try {
            SeckillService proxy = applicationContext.getBean(SeckillService.class);
            proxy.deal(user.getId(), id);
        } finally {
            redisUtil.delLock(lockKey, lockValue);
        }
        return ResponseResult.success("下单成功！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deal(Long userId, Long seckillId) {
        seckillMapper.updateByCondition(seckillId);
        orderMapper.insert(new OrderEntity()
                .setUserId(userId)
                .setSeckillId(seckillId));
    }
}
