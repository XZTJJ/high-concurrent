package org.example.Serialization.json;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.example.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonClient.class);


    public static void main(String[] args) {
        runClient();
    }

    //运行方法
    public static void runClient() {
        EventLoopGroup worker = null;
        try {
            //获取地址和端口
            String ipAddr = ConfigUtils.getAddr();
            int port = ConfigUtils.getPort();
            //设置组件
            Bootstrap bootstrap = new Bootstrap();
            //设置反应线程组,监听线程，默认单线程就好
            worker = new NioEventLoopGroup(1);
            //组装
            bootstrap.group(worker);
            //设置组件的通道类型
            bootstrap.channel(NioSocketChannel.class);
            //设置地址
            bootstrap.remoteAddress(ipAddr, port);
            //设置参数
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //对工作线程 设置 入站 和出站 的handler ,一般情况下都是两个左右
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
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
            ChannelFuture Future = bootstrap.connect();
            Future.sync();
            Channel channel = Future.channel();
            LOGGER.info(" 客户端连接成功 : " + channel.localAddress());
            //开始发送数据
            JsonMsg[] strContent = new JsonMsg[]{
                    new JsonMsg(0, 0, 99, "辰南 : 这是哪里?"),
                    new JsonMsg(1, 1, 99, "龙宝宝 : 这是哪里?"),
            };
            for (int i = 0; i < strContent.length; i++) {
                byte[] bytes = strContent[i].converToJson().getBytes(ConfigUtils.getCodeTyep());
                //在调用解码器或出站队列的时候，会自动释放byteBuf的
                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
                byteBuf.writeInt(bytes.length);
                byteBuf.writeBytes(bytes);
                channel.write(byteBuf);
            }
            channel.flush();
            //优雅的关闭通道,就不阻塞了
            Future.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("客户端运行错误", e);
        } finally {
            if (worker != null)
                worker.shutdownGracefully();
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
