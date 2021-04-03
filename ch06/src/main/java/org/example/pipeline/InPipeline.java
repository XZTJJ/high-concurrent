package org.example.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *   入站流水线
 *   其实在 入站 的hanlder中已经使用过了
 *   org.example.handler这个包下面
 */
public class InPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(InPipeline.class);

    public static void main(String[] args) {
        //通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                ch.pipeline().addLast(new InHandlerA());
                ch.pipeline().addLast(new InHandlerB());
                ch.pipeline().addLast(new InHandlerC());
            }
        };
        //测试数据
        EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
        //缓冲区分配
        ByteBuf byteBuf = Unpooled.buffer();
        //输入的写入
        byteBuf.writeInt(2);
        channel.writeInbound(byteBuf);
        channel.flush();
        channel.writeInbound(byteBuf);
        channel.flush();
        //通道关闭
        channel.close();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 先声明几个入栈handler
     */
    private static class InHandlerA extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "读方法被调用");
            super.channelRead(ctx, msg);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "添加方法被调用");
            super.handlerAdded(ctx);
        }
    }

    private static class InHandlerB extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "读方法被调用");
            super.channelRead(ctx, msg);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "添加方法被调用");
            super.handlerAdded(ctx);
        }
    }

    private static class InHandlerC extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "读方法被调用");
            super.channelRead(ctx, msg);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "添加方法被调用");
            super.handlerAdded(ctx);
        }
    }
}
