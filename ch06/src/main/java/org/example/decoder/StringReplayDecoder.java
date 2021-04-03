package org.example.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 *  自己手写的 解析 字符串的包
 *  和解析Interger非常的类似,
 *  这种方式性能比较较差
 */
public class StringReplayDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringReplayDecoder.class);

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
                    ch.pipeline().addLast(new Byte2StringHandler());
                    ch.pipeline().addLast(new StringHandler());
                }
            };
            //通道开始测试
            EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
            //测试数据
            String content = "神死了……魔灭了……我还活着……为什么？为何让我从远古神墓中复出，我将何去何从？";
            //获取缓冲区
            for (int i = 0; i < 3; i++) {
                int rand = RandomUtils.nextInt(1000,1600);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < rand; j++)
                    sb.append(content);
                sb.append(" , 重复次数为").append(rand);
                LOGGER.info(sb.toString());
                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
                byte[] bytesCont = sb.toString().getBytes("utf-8");
                byteBuf.writeInt(bytesCont.length);
                byteBuf.writeBytes(bytesCont);
                //通道开始写入,缓冲区需要先+1才行
                channel.writeInbound(byteBuf);
            }
            //通道阻塞一直到关闭为止
            channel.close().channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 转态声明，枚举类型就好
     */
    private static enum Status {
        LENGTH, CONTENT;
    }

    /**
     * 字节数组转换成整数，需要继承一个 ByteToMessageDecoder
     *  ByteToMessageDecoder该类是负责字节数组转换成 POJO的
     *  重写 decode  方法就行,并且将转换结果放入到 out 结果中就行
     */
    private static class Byte2StringHandler extends ReplayingDecoder<Status> {
        //声明两个变量
        private int length;
        private byte[] content;

        protected Byte2StringHandler() {
            super(Status.LENGTH);
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            //首先需要获取状态
            switch (state()) {
                case LENGTH:
                    //获取整数
                    length = in.readInt();
                    content = new byte[length];
                    //修改状态
                    checkpoint(Status.CONTENT);
                    break;
                case CONTENT:
                    //读取数字,并且获取整数
                    in.readBytes(content, 0, length);
                    //需要满足条件才能转成第一状态
                    checkpoint(Status.LENGTH);
                    out.add(new String(content, "utf-8"));
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 编写一个 入站 的handler处理器
     *  理论上来说，只需要重写 read 方法就好
     *
     */
    private static class StringHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String result = (String) msg;
            LOGGER.info("------------------------------------------");
            LOGGER.info(result);
            System.out.println();
            super.channelRead(ctx, msg);
        }
    }
}
