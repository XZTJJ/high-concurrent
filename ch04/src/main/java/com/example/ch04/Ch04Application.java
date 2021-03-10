package com.example.ch04;

import com.example.ch04.utils.SpringContextUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

//单线程版本的reactor反应模式
@SpringBootApplication
public class Ch04Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext runContext = SpringApplication.run(Ch04Application.class, args);
        //注SprinBoot运行的上下文
        SpringContextUtils.initContext(runContext);
    }

}
