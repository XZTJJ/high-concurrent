package com.example.ch03client.fileNio;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * FileIO相关，自己写的
 *  存在一个非常大的bug, 如果文件大小为G基本，
 */
@Component("mySleftFileNIO")
public class MySleftFileNIO {

    private static final Logger LOG = LoggerFactory.getLogger(MySleftFileNIO.class);

    @Autowired
    private Environment environment;

    //赋值文件
    public void copyFile() {
        long startTime = System.currentTimeMillis();
        //
        String copyFile = environment.getProperty("custom.copy.file");
        String destFile = environment.getProperty("custom.dest.file");
        if (StringUtils.isBlank(copyFile) || StringUtils.isBlank(destFile)) {
            LOG.error("配置信息错误");
            return;
        }
        FileInputStream fileInputStream = null;
        FileChannel fileInChannel = null;
        FileOutputStream fileOutputStream = null;
        FileChannel fileOutChannel = null;
        //开始复制
        try {
            fileInputStream = new FileInputStream(copyFile);
            //创建通道,一次性读写完
            fileInChannel = fileInputStream.getChannel();
            int bufferSize = Integer.valueOf(environment.getProperty("custom.buffer.size", "1024"));
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            int length = -1;
            //复制文件
            fileOutputStream = new FileOutputStream(destFile);
            fileOutChannel = fileOutputStream.getChannel();
            long total = 0;
            //循环读取
            while ((length = fileInChannel.read(byteBuffer)) != -1) {
                //缓冲区读转换
                byteBuffer.flip();
                int outlength = 0;
                //将buf写入到输出的通道
                while ((outlength = fileOutChannel.write(byteBuffer)) != 0) {
                    total += outlength;
                    if (total % (bufferSize * 200) == 0)
                       LOG.info("写入字节数：" + total);
                }
                byteBuffer.clear();
            }
            //强制刷新
            fileOutChannel.force(true);
            long endTime = System.currentTimeMillis();
            LOG.info("复制完成,耗时:" + (endTime - startTime));
        } catch (Exception e) {
            LOG.error("文件复制失败", e);
        } finally {
            if (fileOutChannel != null)
                try {
                    fileInChannel.close();
                } catch (IOException e) {
                    LOG.error("关闭通道失败", e);
                }

            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LOG.error("关闭通道失败", e);
                }

            if (fileInChannel != null)
                try {
                    fileInChannel.close();
                } catch (IOException e) {
                    LOG.error("关闭通道失败", e);
                }

            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOG.error("关闭通道失败", e);
                }
        }

    }
}
