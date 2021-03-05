package com.example.ch03server.pojo;

import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;

public class TcpServerPROPOJO {
    //文件名
    private String fileName;
    //文件长度
    private long fileSize = -1;
    //文件传输地址
    private InetSocketAddress remoteAddr;
    //文件传输通道
    private FileChannel fileChannel;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public InetSocketAddress getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(InetSocketAddress remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }
}
