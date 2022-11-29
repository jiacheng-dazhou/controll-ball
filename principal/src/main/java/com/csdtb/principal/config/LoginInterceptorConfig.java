package com.csdtb.principal.config;

import com.csdtb.principal.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-15
 **/
@Configuration
public class LoginInterceptorConfig implements WebMvcConfigurer {

    private static String[] PATH_PATTERNS = {"/**"};
    private static String[] EXCLUDE_PATTERNS = {"/user-login/toLogin","/exam-records/selectExamRecordVideo"};

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns(PATH_PATTERNS).excludePathPatterns(EXCLUDE_PATTERNS);
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
