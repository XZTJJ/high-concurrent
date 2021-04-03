package org.example.Serialization.json;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.example.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 *  入站  和 出站 处理程序
 *  处理 字符串 和 字节数组 的转换
 */
public class ByteBufMutStringHandler extends MessageToMessageCodec<ByteBuf, String> {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufMutStringHandler.class);


    //将 String 转换成 ByteBuf
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        //因为已经确定 所有的内容都是 Json 字符串的，直接转成ByteBuf
        ByteBuf byteBuf = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), Charset.forName(ConfigUtils.getCodeTyep()));
        LOGGER.info("字符串 入站编码成 ByteBuf : " + byteBuf);
        out.add(byteBuf);
    }

    //将 ByteBuf 转换成 String
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        //因为已经确定 所有的内容都是构成 Json 字符串的，所以直接可以转成String
        msg.readerIndex(ConfigUtils.getPreLength());
        String content = msg.toString(Charset.forName(ConfigUtils.getCodeTyep()));
        LOGGER.info("ByteBuf 入站解码成 字符串 : " + content);
        out.add(content);
    }
}
