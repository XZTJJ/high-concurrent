package org.example.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *  手写的出站 的测试内
 */
public class MySelfOutHandlerTester {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(MySelfOutHandlerTester.class);

    public static void main(String[] args) {
        //通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                ch.pipeline().addLast(new MySelfOutHandler());
            }
        };
        //模拟出站
        EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
        //分配缓冲区
        ByteBuf byteBuf = Unpooled.buffer();
        //获取数据
        byteBuf.writeInt(1);
        //通道的写入事件
        ChannelFuture channelFuture = channel.writeAndFlush(byteBuf);
        channelFuture.addListener((future) -> {
            if (future.isSuccess())
                LOGGER.info("客户端写入完成");
            //因为不知道什么写入完成,所有是在换掉的时候关闭通道的,不能再外边关闭通道,因为有可能写入还没有完成了
            channel.close();
        });

        //休眠时间
        try {
            TimeUnit.MINUTES.sleep(10);
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }

    }
}
