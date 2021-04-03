package org.example.Serialization.protobuf;

/**
 * 简单的打印数据,用于打印 Proto 中的内容
 */
public class ProtoMsgUtil {

    public static String toString(ProtoMsg.Msg protoMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ version : ").append(protoMsg.getVersion()).append(" , fversion : ").append(protoMsg.getFversion())
                .append(" , magicEnum : ").append(protoMsg.getMagic().getMagicEnum()).append(" , desc : ").append(protoMsg.getMagic().getDesc())
                .append(" , content : ").append(protoMsg.getContent());
        return sb.toString();
    }
}
