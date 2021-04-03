package org.example.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.example.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 *   使用基础的ByteToMessage来实现字符串的分离,
 * 通过上传文件的形式
 *
 */
public class StringByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringByteToMessageDecoder.class);

    public static void main(String[] args) {
        testerMethod();
    }

    //测试方法
    private static void testerMethod() {
        try {
            //通道 初始化
            ChannelInitializer<EmbeddedChannel> channelChannelInitializer = new ChannelInitializer<EmbeddedChannel>() {
                @Override
                protected void initChannel(EmbeddedChannel ch) throws Exception {
                    ch.pipeline().addLast(new Byte2StringHandler());
                    ch.pipeline().addLast(new ByteHandler());
                }
            };
            //通道开始测试
            EmbeddedChannel channel = new EmbeddedChannel(channelChannelInitializer);
            //获取文件通道
            String filePath = StringByteToMessageDecoder.class.getClassLoader().getResource(ConfigUtils.getBook()).getPath();
            FileChannel fileChannel = new FileInputStream(filePath).getChannel();
            long size = fileChannel.size();
            LOGGER.info("文件总大小为 : " + size);
            //声明长度
            long index = 0;
            //声明缓冲区
            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
            while (index < size) {
                //长度
                int readCount = fileChannel.read(byteBuffer);
                index += readCount;
                //获取内容数组
                byte[] content = byteBuffer.array();
                //声明netty的发送数组,
                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
                byteBuf.writeInt(readCount);
                byteBuf.writeBytes(content, 0, readCount);
                channel.writeInbound(byteBuf);
                //清空数据,java缓冲区中的数据
                byteBuffer.clear();
            }
            //文件通道关闭
            fileChannel.close();
            //通道阻塞一直到关闭为止
            channel.close().channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 字节数组转换成整数，需要继承一个 ByteToMessageDecoder
     *  ByteToMessageDecoder该类是负责字节数组转换成 POJO的
     *  重写 decode  方法就行,并且将转换结果放入到 out 结果中就行
     */
    private static class Byte2StringHandler extends ByteToMessageDecoder {
        //声明总共接收的长度
        private long length;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            //首先必须获取长度才行,如果长度都没有传递过来，证明不需要走后面的流程
            if (in.readableBytes() < 4)
                return;

            //先备份，如果长度不够，就可以直接回退了
            in.markReaderIndex();
            //获取长度
            int size = in.readInt();
            //判断长度是否足够
            if (in.readableBytes() < size) {
                //直接可以回退了
                in.resetReaderIndex();
                return;
            }
            //创建数组
            byte[] content = new byte[size];
            in.readBytes(content, 0, size);
            length += size;
            LOGGER.info("总共接收长度为 : " + length);
            //数据传递
            out.add(content);
        }
    }

    /**
     * 编写一个 入站 的handler处理器
     *  理论上来说，只需要重写 read 方法就好
     *
     */
    private static class ByteHandler extends ChannelInboundHandlerAdapter {
        private FileOutputStream fileOutputStream;
        private FileChannel fileChannel;
        private ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //证明是初次复制
            if (fileOutputStream == null) {
                String fullpath = StringByteToMessageDecoder.class.getClassLoader().getResource(ConfigUtils.getBook()).getPath() + "_bak";
                LOGGER.info("生成吗文件路径 : " + fullpath);
                fileOutputStream = new FileOutputStream(fullpath);
                fileChannel = fileOutputStream.getChannel();
                LOGGER.info("文件创建完成");
            }
            //开始转换
            byte[] content = (byte[]) msg;
            //没有数据直接返回
            if (content == null)
                return;
            //将内容写入文件
            byteBuffer.clear();
            byteBuffer.put(content);
            byteBuffer.flip();
            fileChannel.write(byteBuffer);
            super.channelRead(ctx, msg);
        }
    }
}
