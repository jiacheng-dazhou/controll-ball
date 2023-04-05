package com.csdtb.principal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    @Test
    public void testPrintf(){
        A a = new A();
        Thread threadA = new Thread(a, "A");
        Thread threadB = new Thread(a, "B");
        threadA.start();
        threadB.start();
    }

    class A implements Runnable{
        private int i = 0;

        @Override
        public void run() {
            while (true){
                synchronized (this) {
                    notify();
                    if(i < 100){
                        i++;
                        System.out.println(Thread.currentThread().getName()+"---"+i);
                    }else{
                        break;
                    }
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
