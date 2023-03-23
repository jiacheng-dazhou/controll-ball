package com.csdtb.principal;

import org.springframework.stereotype.Service;

/**
 * @author zhoujiacheng
 * @date 2023-02-28
 */
@Service
public class JDKDynamicProxyTargetImpl implements JDKDynamicProxyTarget{
    @Override
    public void show() {
        System.out.println("JDKDynamicProxy...");
    }
}
