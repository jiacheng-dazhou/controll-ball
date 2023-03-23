package com.csdtb.principal.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoujiacheng
 * @date 2023-03-07
 */
public class RedisUtil {

    private  RedisTemplate redisTemplate;

    public RedisUtil(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final DefaultRedisScript<Long> UNLOCK;

    static{
        UNLOCK = new DefaultRedisScript<>();
        UNLOCK.setLocation(new ClassPathResource("delLock.lua"));
        UNLOCK.setResultType(Long.class);
    }

    public boolean tryLock(String key,String value){
       return redisTemplate.opsForValue().setIfAbsent(key,value,10, TimeUnit.SECONDS);
    }

    public boolean tryLock(String key,String value,Long time,TimeUnit timeUnit){
        return redisTemplate.opsForValue().setIfAbsent(key,value,time, timeUnit);
    }

    public void delLock(String key,String value){
        redisTemplate.execute(UNLOCK, Collections.singletonList(key),value);
    }
}
