package com.example.ch03server.controller;

import com.example.ch03server.Utils.SpringContextUtils;
import com.example.ch03server.tcpServer.MySelfTcpServer;
import com.example.ch03server.tcpServer.TcpServerPro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nioServerController/")
public class NioServerController {
    private final static Logger LOGGER = LoggerFactory.getLogger(NioServerController.class);

    //自己实现的socket服务进程相关,
    @RequestMapping("/mySelftTcpServer")
    public void mySelftTcpServer() {
        MySelfTcpServer mySelfTcpServer = SpringContextUtils.getBean(MySelfTcpServer.class);
        mySelfTcpServer.mySelftTcpServer();
    }

    //书上的socket服务进程相关,
    @RequestMapping("/tcpServerPro")
    public void tcpServerPro() {
        TcpServerPro tcpServerPro = SpringContextUtils.getBean(TcpServerPro.class);
        tcpServerPro.tcpServerpro();
    }
}
