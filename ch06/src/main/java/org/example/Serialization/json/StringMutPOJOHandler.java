package org.example.Serialization.json;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

/**
 *  入站  和 出站 处理程序
 *  处理 字符串 和 POJO 的转换
 */
public class StringMutPOJOHandler extends MessageToMessageCodec<String, JsonMsg> {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(StringMutPOJOHandler.class);


    //POJO 转 String
    @Override
    protected void encode(ChannelHandlerContext ctx, JsonMsg msg, List<Object> out) throws Exception {
        //开始转换
        String jsonStr = msg.converToJson();
        LOGGER.info("JsonMsg 入站编码成  String : " + jsonStr);
        out.add(jsonStr);
    }

    //String 转 POJO
    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        //String直接转换成 JsonMsg就好
        JsonMsg jMsg = JsonMsg.jsonConverPOJO(msg);
        LOGGER.info("String 入站解码成 JsonMsg : " + jMsg);
        out.add(jMsg);
    }
}
