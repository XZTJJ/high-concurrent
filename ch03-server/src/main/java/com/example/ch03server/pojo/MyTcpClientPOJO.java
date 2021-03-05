package com.example.ch03server.pojo;


import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * 客户端上传文件保存的类
 */
public class MyTcpClientPOJO {
    //文件的名字
    private String name;
    //文件的总长度
    private long size = -1;
    //文件的大小现有长度
    private long currentSize = 0;
    //文件通道,感觉这么保存存在问题
    private FileChannel fileChannel;
    //文件描述符
    private FileOutputStream fileOutputStream;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public FileOutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public void setFileOutputStream(FileOutputStream fileInputStream) {
        this.fileOutputStream = fileInputStream;
    }

    public void closeFile() {
        IOUtils.closeQuietly(fileChannel);
        IOUtils.closeQuietly(fileOutputStream);
    }
}
