package org.example.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 入栈测试类
 */
public class MySelftInHandlerTester {
    //日志信息
    private static final Logger LOGGER = LoggerFactory.getLogger(MySelftInHandler.class);

    //模拟入栈流程
    public static void main(String[] args) {
        //模拟通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                //往流水线中添加处理器
                ch.pipeline().addLast(new MySelftInHandler());
            }
        };
        //模拟测试
        EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
        //创建缓冲区
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1);
        channel.writeInbound(buffer);
        //强制刷新，防止出现半包问题
        channel.flush();
        //再次写入
        channel.writeInbound(buffer);
        //通道关闭
        channel.close();
        //休眠时间
        try {
            TimeUnit.MINUTES.sleep(10);
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }
    }
}
