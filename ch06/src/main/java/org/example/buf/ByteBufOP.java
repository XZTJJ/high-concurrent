package org.example.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  自己写的简单的ByteBuf
 *  的一些操作信息
 */
public class ByteBufOP {
    //日志相关
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufOP.class);

    public static void main(String[] args) {
        int size = 10;
        //开始分配缓冲区,使用池分配的技术
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(9, 100);
        //开始尽心分配
        print("初始化时", buffer);
        //写入四个字节
        for (int i = 0; i < size; i++)
            buffer.writeInt(i);
        print("写入" + size + "个整数后", buffer);
        //使用get方式获取
        for (int i = 0; i < size; i++)
            LOGGER.info("使用get方式读数据 : " + buffer.getInt(i * 4));
        print("get" + size + "个整数后", buffer);
        //使用readt方式获取
        for (int i = 0; i < size; i++)
            LOGGER.info("使用read方式读数据 : " + buffer.readInt());
        print("read" + size + "个整数后", buffer);

        //增加一个引用数据
        buffer.retain();
        print("增加1个引用计数", buffer);
        buffer.release(2);
        print("删除2个引用计数", buffer);
    }


    //私有的打印属性
    private static void print(String action, ByteBuf byteBuf) {
        LOGGER.info("在 ------------" + action + " ------------");
        LOGGER.info("byteBuf 是否可读 " + byteBuf.isReadable() + " ------------");
        LOGGER.info("byteBuf 读指针 " + byteBuf.readerIndex() + " ------------");
        LOGGER.info("byteBuf 剩余可读 " + byteBuf.readableBytes() + " ------------");
        LOGGER.info("byteBuf 是否可写 " + byteBuf.isWritable() + " ------------");
        LOGGER.info("byteBuf  写指针 " + byteBuf.writerIndex() + " ------------");
        LOGGER.info("byteBuf  剩余可写 " + byteBuf.writableBytes() + " ------------");
        LOGGER.info("byteBuf  初始化容量 " + byteBuf.capacity() + " ------------");
        LOGGER.info("byteBuf  最大容量 " + byteBuf.maxCapacity() + " ------------");
        LOGGER.info("byteBuf   最多可写 " + byteBuf.maxWritableBytes() + " ------------");
        LOGGER.info("byteBuf  的引用数为 " + byteBuf.refCnt() + " ------------\n");
    }
}
