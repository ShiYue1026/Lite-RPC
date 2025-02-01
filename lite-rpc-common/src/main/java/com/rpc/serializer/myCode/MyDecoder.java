package com.rpc.serializer.myCode;

import com.rpc.message.MessageType;
import com.rpc.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class MyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() < 8) { // 确保至少有长度字段 + 消息类型 + 序列化方式
            return;
        }

        byteBuf.markReaderIndex(); // 记录当前读取位置

        // 读取数据包总长度
        int totalLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < totalLength) {
            byteBuf.resetReaderIndex(); // 数据不够，回滚
            return;
        }

        // 读取消息类型
        int messageType = byteBuf.readShort();
        if (messageType != MessageType.REQUEST.getCode() && messageType != MessageType.RESPONSE.getCode() && messageType != MessageType.HEARTBEAT.getCode()) {
            throw new RuntimeException("MyDecoder暂不支持此种数据");
        }

        // 读取序列化器类型
        int serializerType = byteBuf.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if (serializer == null) {
            throw new RuntimeException("不存在对应的序列化器");
        }

        // 读取数据长度
        int length = byteBuf.readInt();
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex(); // 还原读取位置，等待更多数据
            return;
        }

        // 读取数据并反序列化
        byte[] data = new byte[length];
        byteBuf.readBytes(data);
        Object obj = serializer.deserialize(data, messageType);
        out.add(obj);
    }
}