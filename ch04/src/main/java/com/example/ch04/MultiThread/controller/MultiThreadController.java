package com.example.ch04.MultiThread.controller;

import com.example.ch04.MultiThread.client.MultiThreadClient;
import com.example.ch04.MultiThread.client.MultiThreadClientPro;
import com.example.ch04.MultiThread.server.mySelf.MultiThreadReactor;
import com.example.ch04.MultiThread.server.pro.MultiThreadServerReactor;
import com.example.ch04.utils.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多线程版本的Reactor
 */
@RestController
@RequestMapping("/multiThread/")
public class MultiThreadController {
    //日志
    private Logger LOG = LoggerFactory.getLogger(MultiThreadController.class);

    //自己手写的服务端，多线程版本的
    @RequestMapping("/mySelfServer")
    public void mySelfServer() {
        MultiThreadReactor multiThreadReactor = SpringContextUtils.getBean(MultiThreadReactor.class);
        multiThreadReactor.initAndRun();
    }

    //自己手写的客户端，多线程版本的
    @RequestMapping("/mySelfClient")
    public void mySelfClient() {
        MultiThreadClient multiThreadClient = SpringContextUtils.getBean(MultiThreadClient.class);
        multiThreadClient.clientTest();
    }


    //书上给的服务端，多线程版本的
    @RequestMapping("/multiThreadServerReactor")
    public void MultiThreadServerReactor() throws Exception {
        MultiThreadServerReactor multiThreadServerReactor = SpringContextUtils.getBean(MultiThreadServerReactor.class);
        multiThreadServerReactor.startService();
    }

    //书上给的客户端，多线程版本的
    @RequestMapping("/multiThreadClientPro")
    public void MultiThreadClientPro() {
        MultiThreadClientPro multiThreadClientPro = SpringContextUtils.getBean(MultiThreadClientPro.class);
        multiThreadClientPro.clientTest();
    }
}
