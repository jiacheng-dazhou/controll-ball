package com.csdtb.principal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @author zhoujiacheng
 * @date 2023-02-27
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringStudy {

    @Test
    void cglibProxy() {
        AdviceTest advice = new AdviceTest();
        Target target = new Target();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Target.class);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                advice.before();
                Object res = methodProxy.invoke(target, objects);
                advice.after();
                return res;
            }
        });
        Target proxy = (Target) enhancer.create();
        proxy.show();
    }

    @Resource
    private JDKDynamicProxyTarget jdkDynamicProxyTarget;

    @Test
    void jdkDynamicProxy() throws Exception{
        jdkDynamicProxyTarget.show();
    }
}
