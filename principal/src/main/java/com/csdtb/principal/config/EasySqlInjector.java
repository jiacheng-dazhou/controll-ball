package com.csdtb.principal.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

import java.util.List;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-02
 * @Description 自定义数据方法注入
 **/
public class EasySqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        //时间过滤掉，mysql自动更新
        methodList.add(new InsertBatchSomeColumn(item -> !item.getColumn().equals("create_time")&&!item.getColumn().equals("update_time")));
        return methodList;
    }
}
