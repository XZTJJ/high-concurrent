package org.example.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *  自己手写的
 *  将字节数组转换成整数
 */
public class Byte2IntegerDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Byte2IntegerDecoder.class);

    public static void main(String[] args) {
        testerMethod();
    }

    //测试方法
    private static void testerMethod() {
        try {
            //通道 初始化
            ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
                @Override
                protected void initChannel(EmbeddedChannel ch) throws Exception {
                    ch.pipeline().addLast(new Byte2IntegerHandler());
                    ch.pipeline().addLast(new IntegerHandler());
                }
            };
            //通道开始测试
            EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
            //获取缓冲区
            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
            byteBuf.writeInt(10);
            //通道开始写入,缓冲区需要先+1才行
            channel.writeInbound(byteBuf);
            channel.flush();
            LOGGER.info("缓冲区引用数 : " + byteBuf.refCnt());
            byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
            byteBuf.writeInt(50);
            channel.writeInbound(byteBuf);
            channel.flush();
            //通道阻塞一直到关闭为止
            channel.close().channel().closeFuture().sync();
            LOGGER.info("缓冲区引用数 : " + byteBuf.refCnt());
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 字节数组转换成整数，需要继承一个 ByteToMessageDecoder
     *  ByteToMessageDecoder该类是负责字节数组转换成 POJO的
     *  重写 decode  方法就行,并且将转换结果放入到 out 结果中就行
     */
    private static class Byte2IntegerHandler extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            //java中int占4个字节,这里使用read方法，不能使用get方法
            while (in.readableBytes() >= 4)
                out.add(in.readInt());
        }
    }

    /**
     * 编写一个 入站 的handler处理器
     *  理论上来说，只需要重写 read 方法就好
     *
     */
    private static class IntegerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            int result = (int) msg;
            LOGGER.info("解析出来的结果是" + result);
            super.channelRead(ctx, msg);
        }
    }
}
