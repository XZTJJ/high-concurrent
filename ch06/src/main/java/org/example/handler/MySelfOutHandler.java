package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 *  自己手写的 出栈的handler 处理器
 *  继承对应的netty的出栈处理器
 *
 *   出栈和 入栈是非常相似的，可以参考入栈的流程
 *   不过出栈的方法比较少
 */
public class MySelfOutHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySelfOutHandler.class);



    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.bind(ctx, localAddress, promise);
        LOGGER.info(this.getClass().getSimpleName() + " , 绑定完成");
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, promise);
        LOGGER.info(this.getClass().getSimpleName() + " , 绑定完成");
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
        LOGGER.info(this.getClass().getSimpleName() + " , 断开完成");
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
        LOGGER.info(this.getClass().getSimpleName() + " , 关闭完成");
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.deregister(ctx, promise);
        LOGGER.info(this.getClass().getSimpleName() + " , 注册完成");
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 读取完成");
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        LOGGER.info(this.getClass().getSimpleName() + " , 写入完成");
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 刷新完成");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 添加完成");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 移除完成");
    }
}
