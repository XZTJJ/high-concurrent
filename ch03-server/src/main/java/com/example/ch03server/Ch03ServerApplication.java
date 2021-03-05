package com.example.ch03server;

import com.example.ch03server.Utils.SpringContextUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Ch03ServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext runContext = SpringApplication.run(Ch03ServerApplication.class, args);
        //注入上下文
        SpringContextUtils.initContext(runContext);
    }

}
