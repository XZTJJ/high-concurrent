package com.example.ch03client;

import com.example.ch03client.Utils.SpringContextUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Ch03ClientApplication {

    public static void main(String[] args) {
        //获取上下文
        ConfigurableApplicationContext context = SpringApplication.run(Ch03ClientApplication.class, args);
        //初始化上下文
        SpringContextUtils.initContext(context);
    }

}
