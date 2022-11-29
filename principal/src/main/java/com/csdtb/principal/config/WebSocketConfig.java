package com.csdtb.principal.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.util.WebAppRootListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;


/**
 * @Author zhoujiacheng
 * @Date 2022-11-17
 **/
@Configuration
public class WebSocketConfig implements ServletContextInitializer {

    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }

    //设置websocket发送内容长度
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.addListener(WebAppRootListener.class);
        //设置30兆缓冲区
        servletContext.setInitParameter("org.apache.tomcat.websocket.textBufferSize","30000000");
        servletContext.setInitParameter("org.apache.tomcat.websocket.binaryBufferSize","30000000");
    }
}
