package org.example.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 手动写的
 * 使用 netty 自定义 分割符 的解码器
 */
public class NettyDelimiterBasedFrameDecoderTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyDelimiterBasedFrameDecoderTester.class);

    public static void main(String[] args) throws Exception {
        //定义长度
        int length = 1024 * 10;
        //定义分隔符
        String spilt = "\t";
        //内容
        String content = "神死了……魔灭了……我还活着……为什么？为何让我从远古神墓中复出，我将何去何从？";
        //通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                //第一个参数表示最大长度，第二个参数表示是否舍弃分隔符，第三个参数表示分隔符
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(length, true, Unpooled.copiedBuffer(spilt.getBytes("utf-8"))));
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
            int rand = RandomUtils.nextInt(1, 4);
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
