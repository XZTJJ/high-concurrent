package org.example.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *   三种不同的分配策略 :
 *   1.使用 堆分配
 *   2.使用 直接内存 分配
 *   3.混合分配
 */
public class DifAllocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DifAllocator.class);

    public static void main(String[] args) {
        try {
            dumpAllocator();
            directAllocator();
            compAllocator();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    //使用堆分配数据
    private static void dumpAllocator() throws Exception {
        LOGGER.info("堆空间使用");
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        LOGGER.info("神墓啊神墓啊!");
        //写入字节
        byte[] content = "神墓啊神墓啊!".getBytes("utf-8");
        buffer.writeBytes(content);
        if (buffer.hasArray()) {
            //堆空间是可以直接获取的,
            LOGGER.info("堆的使用");
            //因为在堆空间，JVM可以直接操作，所以可以直接获取数据
            byte[] array = buffer.array();
            //终点不用说都是 readableBytes, 不过起点是 读指针 + 数组的偏移量，因为有可能存在数组的偏移量
            LOGGER.info(new String(array, buffer.arrayOffset() + buffer.readerIndex(), buffer.readableBytes(), "utf-8"));
        } else {
            LOGGER.info("非堆的使用");
            //非堆空间，需要先把 非堆空间的数据 复制到 对空间，这样，这样JVM才能识别
            byte[] array = new byte[buffer.readableBytes()];
            //直接空间不需要 数组的偏移量 , 可能的原因是操作系统不支持
            buffer.getBytes(buffer.readerIndex(), array);
            LOGGER.info(new String(array, "utf-8"));
        }
        //缓冲区回收
        buffer.release();
        System.out.println();
    }


    //使用 直接空间 分配数据
    private static void directAllocator() throws Exception {
        LOGGER.info("直接空间使用");
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        LOGGER.info("神墓啊神墓啊!");
        //写入字节
        byte[] content = "神墓啊神墓啊!".getBytes("utf-8");
        buffer.writeBytes(content);
        if (buffer.hasArray()) {
            //堆空间是可以直接获取的,
            LOGGER.info("堆的使用");
            //因为在堆空间，JVM可以直接操作，所以可以直接获取数据
            byte[] array = buffer.array();
            //终点不用说都是 readableBytes, 不过起点是 读指针 + 数组的偏移量，因为有可能存在数组的偏移量
            LOGGER.info(new String(array, buffer.arrayOffset() + buffer.readerIndex(), buffer.readableBytes(), "utf-8"));
        } else {
            LOGGER.info("非堆的使用");
            //非堆空间，需要先把 非堆空间的数据 复制到 对空间，这样，这样JVM才能识别
            byte[] array = new byte[buffer.readableBytes()];
            //直接空间不需要 数组的偏移量 , 可能的原因是操作系统不支持
            buffer.getBytes(buffer.readerIndex(), array);
            LOGGER.info(new String(array, "utf-8"));
        }
        //缓冲区回收
        buffer.release();
        System.out.println();
    }

    //混合空间，混合空间可以组合或者复用 其他的缓冲区
    private static void compAllocator() throws Exception {
        LOGGER.info("混合空间使用");
        CompositeByteBuf comByte = ByteBufAllocator.DEFAULT.compositeBuffer();
        LOGGER.info("题目 : 神墓啊神墓啊!");
        //头部字节的写入
        byte[] head = "题目 : ".getBytes("utf-8");
        ByteBuf headBuff = ByteBufAllocator.DEFAULT.buffer();
        headBuff.writeBytes(head);
        //内容字节的写入
        byte[] content = "神墓啊神墓啊!".getBytes("utf-8");
        ByteBuf contentBuff = ByteBufAllocator.DEFAULT.directBuffer();
        contentBuff.writeBytes(content);
        //组合
        comByte.addComponent(headBuff);
        comByte.addComponent(contentBuff);
        //读取数据
        byte[] readBytes = null;
        StringBuilder sb = new StringBuilder();
        for (ByteBuf c : comByte) {
            readBytes = new byte[c.readableBytes()];
            c.getBytes(c.readerIndex(), readBytes);
            sb.append(new String(readBytes, "utf-8"));
        }
        LOGGER.info("获取的数据为 ：" + sb.toString());
        //释放空间,会把comByte里面的所有会被释放掉，因为headBuff后面需要用到，所以先+1
        headBuff.retain();
        comByte.release();
        //重新分配
        contentBuff = ByteBufAllocator.DEFAULT.directBuffer();
        comByte = ByteBufAllocator.DEFAULT.compositeBuffer();
        content = "长生界啊长生界啊! : ".getBytes("utf-8");
        contentBuff.writeBytes(content);
        //组合
        comByte.addComponent(headBuff);
        comByte.addComponent(contentBuff);
        //读取数据
        sb = new StringBuilder();
        for (ByteBuf c : comByte) {
            readBytes = new byte[c.readableBytes()];
            c.getBytes(c.readerIndex(), readBytes);
            sb.append(new String(readBytes, "utf-8"));
        }
        LOGGER.info("获取的数据为 ：" + sb.toString());

        //缓冲区回收
        comByte.release();
    }
}
