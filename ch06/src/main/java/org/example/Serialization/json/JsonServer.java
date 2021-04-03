package org.example.Serialization.json;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.example.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  使用Json序列化的服务端
 *
 *  约定的数据格式为 : 长度(int) + 字符串(String)
 */
public class JsonServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServer.class);

    public static void main(String[] args) {
        runServer();
    }

    //运行方法
    public static void runServer() {
        EventLoopGroup boss = null;
        EventLoopGroup worker = null;
        try {
            int port = ConfigUtils.getPort();
            //设置组件
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //设置反应线程组,监听线程，默认单线程就好
            boss = new NioEventLoopGroup(1);
            //工作线程，使用默认的 CPU * 2 个线程处理
            worker = new NioEventLoopGroup();
            //组装
            serverBootstrap.group(boss, worker);
            //设置组件的通道类型
            serverBootstrap.channel(NioServerSocketChannel.class);
            //设置地址
            serverBootstrap.localAddress(port);
            //设置参数
            serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //对工作线程 设置 入站 和出站 的handler ,一般情况下都是两个左右
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    //首先需要肯定的是 入站 和 出站 都需要添加，注意 入站 和 出站 他们之间的顺序
                    //第一个执行 出站 解码器
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(ConfigUtils.getStrLength(), 0, ConfigUtils.getPreLength(), 0, 4));
                    //第一个执行 出站 解码器
                    ch.pipeline().addLast(new LengthFieldPrepender(ConfigUtils.getPreLength()));
                    //第二个 执行 的 入站 和 出站 解码器
                    ch.pipeline().addLast(new ByteBufMutStringHandler());
                    //第三个 执行的 入站 和 出站 解码器
                    ch.pipeline().addLast(new StringMutPOJOHandler());
                    //第四个 执行的 入站 和 出站 的Handler了，这里直接简单的打印一下就好, 直接使用内部类的形式就好
                    ch.pipeline().addLast(new EasyInHandler());
                    ch.pipeline().addLast(new EasyOutHandler());
                }
            });
            //地址和端口绑定，并且启动该服务器
            ChannelFuture Future = serverBootstrap.bind().sync();
            LOGGER.info(" 服务器启动成功，监听端口: " + Future.channel().localAddress());
            //优雅的关闭通道,就不阻塞了
            ChannelFuture closeFuture = Future.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            LOGGER.error("服务器运行错误", e);
        } finally {
            if (worker != null)
                worker.shutdownGracefully();
            if (boss != null)
                boss.shutdownGracefully();
        }

    }


    /**
     * 简单的 入站 的handler
     *  简单打印一下，模拟 入站 业务处理
     */
    private static class EasyInHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //打印
            LOGGER.info("模拟 入站 处理逻辑 , " + msg.toString());
            super.channelRead(ctx, msg);
            //然后在出站,数据写回的逻辑
            byte[] bytes = ((JsonMsg) msg).converToJson().getBytes(ConfigUtils.getCodeTyep());
            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
            ctx.channel().write(byteBuf);
            ctx.channel().flush();
        }
    }

    /**
     * 简单的 出站 的handler
     *  简单打印一下，模拟 出站 业务处理
     */
    private static class EasyOutHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            //打印
            LOGGER.info("模拟 出站 处理逻辑 , " + msg.toString());
            super.write(ctx, msg, promise);
        }
    }
}
