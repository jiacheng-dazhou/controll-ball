package com.csdtb.principal.interceptor;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.csdtb.common.constant.ResponseType;
import com.csdtb.common.dto.user.UserDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
        //token过期，拒绝访问
        if (token == null) {
            setResp(response);
            return false;
        }
        UserDTO user = (UserDTO)redisTemplate.opsForValue().get(token);
        if (user == null) {
            setResp(response);
            return false;
        }
        String userLoginKey = "Login_User:" + user.getId();
        //刷新token过期时间
        if (!(redisTemplate.expire(token, 2 * 60 * 60, TimeUnit.SECONDS) && redisTemplate.expire(userLoginKey,2 * 60 * 60, TimeUnit.SECONDS))) {
            setResp(response);
            return false;
        }
        return true;
    }

    private void setResp(HttpServletResponse response) {
        try {
            response.sendError(ResponseType.REQUEST_FORBIDDEN.getCode(), ResponseType.REQUEST_FORBIDDEN.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
