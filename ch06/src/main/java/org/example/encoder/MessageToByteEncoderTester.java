package org.example.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;


/**
 *  自己手写的 编码器
 *  主要是使用 netty 的自带的编码器
 */
public class MessageToByteEncoderTester {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageToByteEncoderTester.class);

    public static void main(String[] args) throws Exception {
        //通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                //这里需要注意,对于出栈而言, 后添加的 反而 先处理
                ch.pipeline().addLast(new Integer2ByteEncoder());
                ch.pipeline().addLast(new String2IntegerEncoder());
            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);

        //开始写入数据
        for (int i = 0; i < 3; i++) {
            channel.write(""+i);
        }
        channel.flush();

        //现在可以读数据了
        ByteBuf serByteBuf = channel.readOutbound();
        while (serByteBuf != null) {
            int charInt = serByteBuf.readInt();
            LOGGER.info("收到的整数为 : " + charInt);
            serByteBuf = channel.readOutbound();
        }
    }

    /**
     *  数据重新修饰一下
     *  String 转换成 String
     */
    private static class String2IntegerEncoder extends MessageToMessageEncoder<String> {
        @Override
        protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
            char[] chars = msg.toCharArray();
            for (char c : chars)
                out.add((int) c);
        }
    }

    /**
     *  数据编码
     */
    private static class Integer2ByteEncoder extends MessageToByteEncoder<Integer> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Integer msg, ByteBuf out) throws Exception {
            out.writeInt(msg);
        }
    }
}
