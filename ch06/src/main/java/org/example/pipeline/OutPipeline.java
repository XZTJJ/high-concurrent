package org.example.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *   出站流水线
 *   其实在 出站 的hanlder中已经使用过了
 *   org.example.handler这个包下面
 */
public class OutPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutPipeline.class);

    public static void main(String[] args) {
        //通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                ch.pipeline().addLast(new OutHandlerA());
                ch.pipeline().addLast(new OutHandlerB());
                ch.pipeline().addLast(new OutHandlerC());
            }
        };
        //测试数据
        EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
        //缓冲区分配
        ByteBuf byteBuf = Unpooled.buffer();
        //输入的写入
        byteBuf.writeInt(2);
        channel.writeOutbound(byteBuf);
        channel.flush();
        channel.writeOutbound(byteBuf);
        channel.flush();
        //添加对应的时间
        try {
            //通道关闭
            channel.close().channel().closeFuture().sync();
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 先声明几个入栈handler
     */
    private static class OutHandlerA extends ChannelOutboundHandlerAdapter {
        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "读方法被调用");
            super.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "写方法被调用");
            super.write(ctx, msg, promise);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "添加方法被调用");
            super.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "删除方法被调用");
            super.handlerRemoved(ctx);
        }
    }

    private static class OutHandlerB extends ChannelOutboundHandlerAdapter {
        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "读方法被调用");
            super.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "写方法被调用");
            super.write(ctx, msg, promise);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "添加方法被调用");
            super.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "删除方法被调用");
            super.handlerRemoved(ctx);
        }
    }

    private static class OutHandlerC extends ChannelOutboundHandlerAdapter {
        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "读方法被调用");
            super.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "写方法被调用");
            super.write(ctx, msg, promise);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "添加方法被调用");
            super.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "删除方法被调用");
            super.handlerRemoved(ctx);
        }
    }
}
