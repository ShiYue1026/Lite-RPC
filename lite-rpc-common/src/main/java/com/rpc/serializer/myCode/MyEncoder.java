package com.rpc.serializer.myCode;

import com.rpc.message.MessageType;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import com.rpc.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder<Object> {

    private final Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf byteBuf) throws Exception {
        log.info("要进行编码的数据的类型: {}", msg.getClass());

        byteBuf.markWriterIndex();  // 记录当前位置
        byteBuf.writeInt(0); // 预留 4 字节存放数据包总长度

        // 写入消息类型
        if (msg instanceof RpcRequest) {
            byteBuf.writeShort(MessageType.REQUEST.getCode());
        } else if (msg instanceof RpcResponse) {
            byteBuf.writeShort(MessageType.RESPONSE.getCode());
        } else {
            throw new IllegalArgumentException("不支持的消息类型: " + msg.getClass());
        }

        // 写入序列化方式
        byteBuf.writeShort(serializer.getType());

        // 写入序列化后的数据
        byte[] bytes = serializer.serialize(msg);
        byteBuf.writeInt(bytes.length); // 写入数据长度
        byteBuf.writeBytes(bytes); // 写入数据内容

        // 计算总长度并回填
        int totalLength = byteBuf.writerIndex() - 4; // 计算当前数据包的总长度（不包括自身长度字段）
        byteBuf.setInt(0, totalLength); // 回填长度字段
    }
}


