package org.example.CompleteNetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.example.utils.ConfigUtils;

/**
 *  自己手写的
 *    netty完成的项目, 丢弃服务
 *    一定要使用到入站，出站的handler,
 *    并且要处理半包问题(粘包,半包)问题
 *    最好使用protobuf来序列化。
 *
 *    留到最后,先把其他的实践先
 */
public class NettyDiscardServer {
    //端口，地址
    private String addr;
    private int port;
    //组装对应的netty的组件
    private ServerBootstrap serverBootstrap;

    public NettyDiscardServer() {
        this.addr = ConfigUtils.getAddr();
        this.port = ConfigUtils.getPort();
    }

    //运行方法
    public void runServer() {
        //创建组件启动器
        serverBootstrap = new ServerBootstrap();
        //两个事件监听器,一个事件监听器负责监控和接受新连接
        EventLoopGroup listenLoop = new NioEventLoopGroup(1);
        //另一个监听器负责处理每个连接的业务处理,创建的线程数为 CPU*2
        EventLoopGroup workerLoop = new NioEventLoopGroup();
        //装配,对应的工作线程和反应组
        serverBootstrap.group(listenLoop, workerLoop);
        //设置通道类型，是tcp的服务器
        serverBootstrap.channel(NioServerSocketChannel.class);
        //设置两个反应器配置信息,心跳检查,已经使用池来分配对应的bytebuf
        serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        //通道初始化,只需要初始化子通道就好
        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {

            @Override
            protected void initChannel(NioSocketChannel channel) throws Exception {

            }
        });

    }
}
