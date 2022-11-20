package com.csdtb.principal;

import com.csdtb.principal.websocket.ExamWebSocket;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-09
 **/
@SpringBootApplication
@MapperScan("com.csdtb.database.mapper")
public class PrincipalApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PrincipalApplication.class);
        ExamWebSocket.setApplicationContext(context);
    }
}
