package com.example.ch03client.fileNio;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * NIO比较快速的读写文件
 */
@Component("fileNIOFastCopy")
public class FileNIOFastCopy {

    private static final Logger LOG = LoggerFactory.getLogger(FileNIOFastCopy.class);

    @Autowired
    private Environment environment;


    //赋值文件
    public void copyFile() {
        long startTime = System.currentTimeMillis();
        //
        String copyFileStr = environment.getProperty("custom.copy.file");
        String destFileStr = environment.getProperty("custom.dest.file");
        int bufferSize = Integer.valueOf(environment.getProperty("custom.buffer.size", "1024"));
        if (StringUtils.isBlank(copyFileStr) || StringUtils.isBlank(copyFileStr)) {
            LOG.error("配置信息错误");
            return;
        }

        File srcFile = new File(copyFileStr);
        File destFile = new File(destFileStr);

        try {
            //如果目标文件不存在，则新建
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            FileInputStream fis = null;
            FileOutputStream fos = null;
            FileChannel inChannel = null;
            FileChannel outChannel = null;
            try {
                fis = new FileInputStream(srcFile);
                fos = new FileOutputStream(destFile);
                inChannel = fis.getChannel();
                outChannel = fos.getChannel();
                long size = inChannel.size();
                System.out.println("文件长度为: " + size + " 字节");
                long pos = 0;
                long count = 0;
                while (pos < size) {
                    //每次复制最多1024个字节，没有就复制剩余的
                    count = size - pos > bufferSize ? bufferSize : size - pos;
                    //复制内存,偏移量pos + count长度
                    pos += outChannel.transferFrom(inChannel, pos, count);
                    if (pos % (bufferSize * 200) == 0)
                        LOG.info("剩余长度" + (size - pos));
                }

                //强制刷新磁盘
                outChannel.force(true);
            } finally {
                IOUtils.closeQuietly(outChannel);
                IOUtils.closeQuietly(fos);
                IOUtils.closeQuietly(inChannel);
                IOUtils.closeQuietly(fis);
            }
            long endTime = System.currentTimeMillis();
            LOG.info("base 复制毫秒数：" + (endTime - startTime));

        } catch (IOException e) {
            LOG.error("复制出现失败", e);
        }
    }
}
