package com.csdtb.principal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-09
 **/
@SpringBootApplication
@MapperScan("com.csdtb.database.mapper")
public class PrincipalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrincipalApplication.class);
    }
}
