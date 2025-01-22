package common.serializer.myCode;

import common.Message.MessageType;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class MyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        // 获取消息类型
        int messageType = byteBuf.readShort();
        if(!Objects.equals(messageType, MessageType.REQUEST.getCode()) &&
        !Objects.equals(messageType, MessageType.RESPONSE.getCode())) {
            throw new RuntimeException("MyDecoder暂不支持此种数据");
        }

        // 获取指定的序列化器
        int serializerType = byteBuf.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if(serializer == null){
            throw new RuntimeException("不存在对应的序列化器");
        }

        // 根据字节流长度读取数据内容并反序列化
        int length = byteBuf.readInt();
        byte[] data = new byte[length];
        byteBuf.readBytes(data);

        Object o = serializer.deserialize(data, messageType);
        out.add(o);
    }
}
