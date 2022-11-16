package com.csdtb.principal.interceptor;

import com.alibaba.fastjson.JSON;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.constant.ResponseType;
import com.csdtb.common.dto.user.UserDTO;
import com.csdtb.database.entity.UserLoginEntity;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-11
 **/

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        //没有登录用户token，重定向到登录页面
        if (token == null) {
            setResp(response);
            return false;
        }
        //刷新token过期时间
        if (!redisTemplate.expire(token,2*60*1000, TimeUnit.SECONDS)) {
            setResp(response);
            return false;
        }
        return true;
    }
    private void setResp(HttpServletResponse response){
        try {
            response.sendError(ResponseType.REQUEST_FORBIDDEN.getCode(),ResponseType.REQUEST_FORBIDDEN.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}