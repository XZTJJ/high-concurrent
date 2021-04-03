package org.example.Serialization.protobuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.example.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自己写的 protobuf 服务端程序
 * 使用 protobuf进行 同行的方式
 *  直接使用 netty 内置的 protobuf 的解码器了
 */
public class ProtoServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoServer.class);

    public static void main(String[] args) {
        runServer();
    }
    //运行方法
    private static void runServer() {
        EventLoopGroup work = null;
        EventLoopGroup boss = null;
        try {
            //端口
            int port = ConfigUtils.getPort();
            //创建 反应器线程组
            work = new NioEventLoopGroup(1);
            boss = new NioEventLoopGroup();
            //创建组件
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //设置 反应器线程组
            serverBootstrap.group(work, boss);
            //设置 通道类型，tcp的模式
            serverBootstrap.channel(NioServerSocketChannel.class);
            //设置需要连接的端口
            serverBootstrap.localAddress(port);
            //通道的配置
            serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //通道的初始化
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    //添加 入站 的解码器,首先是获取二进制文件的长度解码器
                    ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                    //添加 出站 的编码器,首先是获取二进制文件的长度编码器
                    ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                    //添加 入站 的解码器,首先是 protobuf 转成 POJO 解码器, 提供一个参数，用于确定到底是转换成那个对象
                    ch.pipeline().addLast(new ProtobufDecoder(ProtoMsg.Msg.getDefaultInstance()));
                    //添加 入站 的解码器,首先是 protobuf 转成 POJO 解码器
                    ch.pipeline().addLast(new ProtobufEncoder());
                    //添加 入站 的handler
                    ch.pipeline().addLast(new protoEasyInHandler());
                    //添加 出站 的handler
                    ch.pipeline().addLast(new protoEasyOutnHandler());
                }
            });
            //服务器通道绑定
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            LOGGER.info("服务端启动成功, 监听地址为 : " + channelFuture.channel().localAddress());
            //阻塞关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("服务器运行错误", e);
        } finally {
            if (work != null)
                work.shutdownGracefully();
            if (boss != null)
                boss.shutdownGracefully();
        }

    }

    /**
     *  一个简答的 入站 的handler
     */
    private static class protoEasyInHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LOGGER.info("模拟 入站 的handler , 接受的类型为 : " + msg.getClass().getSimpleName() + " , 内容为 : " + ProtoMsgUtil.toString((ProtoMsg.Msg) msg));
            super.channelRead(ctx, msg);
            //出站
            ctx.channel().writeAndFlush(msg);
        }
    }

    /**
     *  一个简答的 出站 的handler
     */
    private static class protoEasyOutnHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            //只是简单的打印一下出站的逻辑
            LOGGER.info("模拟 出站 的handler , 发出的类型为 : " + msg.getClass().getSimpleName() + " , 内容为 : " + ProtoMsgUtil.toString((ProtoMsg.Msg) msg));
            super.write(ctx, msg, promise);
        }
    }

}
