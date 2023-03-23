package com.csdtb.principal.controller;

import com.csdtb.common.ResponseResult;
import com.csdtb.principal.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系模块
 * </p>
 *
 * @author dazhou
 * @since 2023-03-07
 */
@Slf4j
@RestController
@RequestMapping("/seckill")
public class SeckillController {
    @Resource
    private SeckillService seckillService;

    @GetMapping("{id}")
    public ResponseResult seckillWithRedisLock(@PathVariable("id")Long id){
        return seckillService.seckillWithRedisLock(id);
    }
}
