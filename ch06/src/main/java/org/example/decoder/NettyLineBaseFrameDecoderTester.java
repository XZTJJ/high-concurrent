package org.example.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  自己写的用于，
 *  使用netty自带的 \r\n或者\n 的方式分割字符
 */
public class NettyLineBaseFrameDecoderTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyLineBaseFrameDecoderTester.class);

    public static void main(String[] args) throws Exception {
        //定义长度
        int length = 1024 * 10;
        //定义分隔符
        String spilt = "\r\n";
        //内容
        String content = "神死了……魔灭了……我还活着……为什么？为何让我从远古神墓中复出，我将何去何从？";
        //通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                ch.pipeline().addLast(new LineBasedFrameDecoder(length));
                ch.pipeline().addLast(new StringInHandler());
            }
        };
        //通道
        EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
        //开始
        byte[] contByte = content.getBytes("utf-8");
        for (int i = 0; i < 3; i++) {
            //生成缓冲区
            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
            //生成随机数
            int rand = RandomUtils.nextInt(1, 3);
            for (int j = 0; j < rand; j++)
                byteBuf.writeBytes(contByte);
            byteBuf.writeBytes(spilt.getBytes("utf-8"));
            //通道发送
            channel.writeInbound(byteBuf);
        }
        //等待通道关闭
        channel.close().channel().closeFuture().sync();
    }


    /**
     * 手动写入 入站 处理器
     *
     */
    private static class StringInHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            //开始生成数据
            byte[] content = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(content, byteBuf.readerIndex(), byteBuf.readableBytes());
            LOGGER.info(new String(content, "utf-8"));
            System.out.println();
            super.channelRead(ctx, msg);
        }
    }
}
