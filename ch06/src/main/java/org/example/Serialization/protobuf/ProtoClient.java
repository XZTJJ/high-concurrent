package org.example.Serialization.protobuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.example.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自己写的 protobuf 客户端端程序
 * 使用 protobuf进行 同行的方式
 *  直接使用 netty 内置的 protobuf 的解码器了
 */
public class ProtoClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoClient.class);

    public static void main(String[] args) {
        runClient();
    }
    //运行方法
    private static void runClient() {
        EventLoopGroup work = null;
        try {
            //端口 和 地址
            int port = ConfigUtils.getPort();
            String ipAddr = ConfigUtils.getAddr();
            //创建 反应器线程组
            work = new NioEventLoopGroup(1);
            //创建组件
            Bootstrap bootstrap = new Bootstrap();
            //设置 反应器线程组
            bootstrap.group(work);
            //设置 通道类型，tcp的模式
            bootstrap.channel(NioSocketChannel.class);
            //设置需要连接的端口
            bootstrap.remoteAddress(ipAddr, port);
            //通道的配置
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //通道的初始化
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
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
            ChannelFuture channelFuture = bootstrap.connect().sync();
            Channel channel = channelFuture.channel();
            LOGGER.info("客户端连接成功, 连接地址为 : " + channel.localAddress());
            //开始发送信息
            for (int i = 0; i < 2; i++) {
                ProtoMsg.Msg msg = buildProtoMsg(i);
                channel.write(msg);
            }
            channel.flush();
            //阻塞关闭
            channel.closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("服务器运行错误", e);
        } finally {
            if (work != null)
                work.shutdownGracefully();
        }

    }

    //生成 ProtoBuf 的POJO对象存在
    private static ProtoMsg.Msg buildProtoMsg(int i) {
        ProtoMsg.Msg.Builder msgBuilder = ProtoMsg.Msg.newBuilder();
        if (i == 0)
            msgBuilder.setContent("辰南 : 唯一超越逆天级的人物,集结所有生灵的力量");
        else
            msgBuilder.setContent("龙宝宝 : 唯一可以赶上天龙王的龙族存在");
        msgBuilder.setVersion(i).setFversion(i * 10);
        //生成内部类
        ProtoMsg.Msg.Magic.Builder magicBuild = ProtoMsg.Msg.Magic.newBuilder();
        magicBuild.setDesc("嘻嘻.....").setMagicEnum(ProtoMsg.Msg.MagicEnum.DEVPLOP);
        ProtoMsg.Msg.Magic magic = magicBuild.build();
        //内部类的添加
        msgBuilder.setMagic(magic);
        //生成外部内
        ProtoMsg.Msg build = msgBuilder.build();
        return build;
    }

    /**
     *  一个简答的 入站 的handler
     */
    private static class protoEasyInHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LOGGER.info("模拟 入站 的handler , 接受的类型为 : " + msg.getClass().getSimpleName() + " , 内容为 : " + ProtoMsgUtil.toString((ProtoMsg.Msg) msg));
            super.channelRead(ctx, msg);
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
