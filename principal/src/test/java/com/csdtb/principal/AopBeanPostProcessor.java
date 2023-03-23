package com.csdtb.principal;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author zhoujiacheng
 * @date 2023-02-28
 */
@Component
public class AopBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof JDKDynamicProxyTarget){
            AdviceTest advice = new AdviceTest();
            Object proxyInstance = Proxy.newProxyInstance(
                    bean.getClass().getClassLoader(),
                    bean.getClass().getInterfaces(),
                    (Object proxy, Method method, Object[] args) -> {
                        advice.before();
                        Object result = method.invoke(bean, args);
                        advice.after();
                        return result;
                    });
            return proxyInstance;
        }
        return null;
    }
}
