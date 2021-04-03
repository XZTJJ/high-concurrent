package org.example.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 手写的使用 netty 的自带的解析器，进行解析
 *  整数拆包，整数拆包是非常容易拆包的，因为整数只会占用4个字节
 *
 */
public class Byte2IntReplaydecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Byte2IntReplaydecoder.class);

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
            for (int i = 10; i < 100; i += 10) {
                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
                byteBuf.writeInt(i);
                //通道开始写入,缓冲区需要先+1才行
                channel.writeInbound(byteBuf);
                LOGGER.info("缓冲区引用数 : " + byteBuf.refCnt());
            }
            //通道阻塞一直到关闭为止
            channel.close().channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 字节数组转换成整数，需要继承一个 ByteToMessageDecoder
     *  ByteToMessageDecoder该类是负责字节数组转换成 POJO的
     *  重写 decode  方法就行,并且将转换结果放入到 out 结果中就行
     */
    private static class Byte2IntegerHandler extends ReplayingDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            int i = in.readInt();
            out.add(i);
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
