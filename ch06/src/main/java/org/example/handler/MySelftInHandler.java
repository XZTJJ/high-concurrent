package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  自己手写的 入栈的handler 处理器
 *  继承对应的netty的入栈处理器
 */
public class MySelftInHandler extends ChannelInboundHandlerAdapter {
    //日志信息
    private static final Logger LOGGER = LoggerFactory.getLogger(MySelftInHandler.class);
    //直接继承父类的方法

    // 当通道首次 注册到 EventLoop 反应器上时 触发, 只会被调用一次
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 注册成功");
    }

    //当通道从 EventLoop 上移除时, 会被调用 , 只会被调用一次
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 注销完成");
    }

    // 当通道激活 被调用, 这里的激活指的是所有的hanlder都被装配到通道上了, 只会被调用一次
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 激活成功");
    }

    // 当通道被添加到 流水线上 时被调用 , 只会被调用一次
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 添加成功");
    }

    // 当通道从 流水线上 移除时被调用 , 只会被调用一次
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 移除完成");
    }

    //当 通道 处于ESTABLISH 状态或者底层通道关闭 时被调用 , 只会被调用一次
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 失活完成");
    }

    //每次通道可读时，都调用， 不止一次
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        LOGGER.info(this.getClass().getSimpleName() + " , 读取成功");
    }

    //每次通道读完时，都调用， 不止一次
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        LOGGER.info(this.getClass().getSimpleName() + " , 全部读完");
    }

}
