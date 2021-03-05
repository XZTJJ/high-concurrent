package com.example.ch03client.controller;

import com.example.ch03client.Utils.SpringContextUtils;
import com.example.ch03client.fileNio.FileNIOFastCopy;
import com.example.ch03client.fileNio.MySleftFileNIO;
import com.example.ch03client.tcpClient.MySelfTcpClientSocket;
import com.example.ch03client.tcpClient.TcpClientSocketPro;
import com.example.ch03client.udpClient.MySelfUDPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nioClientTest/")
public class NioClientController {
    private final static Logger LOGGER = LoggerFactory.getLogger(NioClientController.class);

    //自己实现的fileNio相关,存在的问题,IO被占用完了
    @RequestMapping("/mySlefFileNIO")
    public void mySlefFileNIO() {
        MySleftFileNIO mySleftFileNIO = (MySleftFileNIO) SpringContextUtils.getBean("mySleftFileNIO");
        mySleftFileNIO.copyFile();
    }


    //书上提供的fileNio相关的实现，兼具速度和IO使用方面
    @RequestMapping("/fileNIOFast")
    public void fileNIOFast() {
        FileNIOFastCopy fileNIOFastCopy = (FileNIOFastCopy) SpringContextUtils.getBean("fileNIOFastCopy");
        fileNIOFastCopy.copyFile();
    }


    //自己手写的，用于tcp上传文件到服务器目录的
    @RequestMapping("/mySelfTcpSocket")
    public void mySelfTcpSocket() {
        MySelfTcpClientSocket mySelfTcpClientSocket = SpringContextUtils.getBean(MySelfTcpClientSocket.class);
        mySelfTcpClientSocket.clientTcpSocket();
    }

    //书上的，用于tcp上传文件到服务器目录的
    @RequestMapping("/tcpClientSocketPro")
    public void tcpClientSocketPro() {
        TcpClientSocketPro tcpClientSocketPro = SpringContextUtils.getBean(TcpClientSocketPro.class);
        tcpClientSocketPro.tcpClientSend();
    }

    //自己的，用于udp上传文件到服务器目录的
    @RequestMapping("/mySelfUDPClient")
    public void mySelfUDPClient() {
        MySelfUDPClient mySelfUDPClient = SpringContextUtils.getBean(MySelfUDPClient.class);
        mySelfUDPClient.mySelfUDPClient();

    }
}
