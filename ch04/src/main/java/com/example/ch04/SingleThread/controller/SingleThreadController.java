package com.example.ch04.SingleThread.controller;

import com.example.ch04.SingleThread.client.ClientPro;
import com.example.ch04.SingleThread.client.MySelfClient;
import com.example.ch04.SingleThread.server.myself.MySelfReactor;
import com.example.ch04.SingleThread.server.pro.ServerReactorPro;
import com.example.ch04.SingleThread.utils.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//单线程的Reactor的测试程序
@RestController
@RequestMapping("/singleThread/")
public class SingleThreadController {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(MySelfClient.class);

    //自己写的 单线程的 reactor 的客户端程序
    @RequestMapping("/mySelfClient")
    public void mySelfReactor() {
        MySelfClient myselfClient = SpringContextUtils.getBean(MySelfClient.class);
        myselfClient.clientTest();
    }

    //自己写的 单线程的 reactor 的客户端程序
    @RequestMapping("/mySelfServer")
    public void MySelfReactor() {
        MySelfReactor mySelfReactor = SpringContextUtils.getBean(MySelfReactor.class);
        mySelfReactor.startedServer();
    }


    //自己写的 单线程的 reactor 的客户端程序
    @RequestMapping("/clientPro")
    public void clientPro() {
        ClientPro clientPro = SpringContextUtils.getBean(ClientPro.class);
        clientPro.clientPro();
    }

    //自己写的 单线程的 reactor 的客户端程序
    @RequestMapping("/serverReactorPro")
    public void serverReactorPro() {
        ServerReactorPro serverReactorPro = SpringContextUtils.getBean(ServerReactorPro.class);
        serverReactorPro.startServerReactorPro();
    }
}
