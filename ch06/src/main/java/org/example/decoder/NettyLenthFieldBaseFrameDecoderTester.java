package org.example.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  稍微复杂结构,其实感觉这个没有什么用处
 *  复杂的直接用json 或者 protobuf 就行了
 *  这样这个就没有存在的必要了
 */
public class NettyLenthFieldBaseFrameDecoderTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyLenthFieldBaseFrameDecoderTester.class);

    public static void main(String[] args) throws Exception {
        //格式为  主版本号(int)+副版本号(int)+长度(int)+魔数(int)+内容(String)
        //定义长度
        int length = 1024 * 10;
        //内容
        String content = "神魔陵园除了安葬着人类历代的最强者、异类中的顶级修炼者外，其余每一座坟墓都埋葬着一位远古的神或魔，这是一片属于神魔的安息之地。 一个平凡的青年死去万载岁月之后，从远古神墓中复活而出，如林的神魔墓碑令青年感到异常震撼。";
        //通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                //第1个参数表示长度，第2个参数表示长度起始偏移量，第3个参数表示长度的位数，第4个参数表示长度的纠正偏移量，第5个参数丢弃的长度
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(length, 8, 4, 4, 16));
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
            //写入主版本号
            byteBuf.writeInt(i);
            //写入副版本号
            byteBuf.writeInt(i * 10);
            //写入长度
            byteBuf.writeInt(contByte.length * rand);
            //写入模式
            byteBuf.writeInt(99);
            for (int j = 0; j < rand; j++)
                byteBuf.writeBytes(contByte);
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
