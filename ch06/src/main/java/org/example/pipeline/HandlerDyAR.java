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
 *  流水线上的 handler动态的拔插
 *   这个放这里主要的目的 是因为需要通过 pipeline 来实现动态
 * 的拔插的
 */
public class HandlerDyAR {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerDyAR.class);

    public static void main(String[] args) {
        //通道初始化
        ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                ch.pipeline().addLast(new InHandlerA());
            }
        };
        //测试数据
        EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
        channel.pipeline().addLast(new InHandlerB());
        channel.pipeline().addLast(new InHandlerC());
        //缓冲区分配
        ByteBuf byteBuf = Unpooled.buffer();
        //输入的写入
        byteBuf.writeInt(2);
        channel.writeInbound(byteBuf);
        channel.flush();
        //移除A吧
        channel.pipeline().remove(InHandlerA.class);
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

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "移除方法被调用");
            super.handlerRemoved(ctx);
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

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "移除方法被调用");
            super.handlerRemoved(ctx);
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

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info(this.getClass().getSimpleName() + "移除方法被调用");
            super.handlerRemoved(ctx);
        }
    }


}
