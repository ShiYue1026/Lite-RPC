package common.serializer.myCode;

import common.Message.MessageType;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {

    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        System.out.println("要进行编码的数据的类型: " + msg.getClass());

        // 写入消息类型
        if(msg instanceof RpcRequest) {
            byteBuf.writeShort(MessageType.REQUEST.getCode());
        } else if(msg instanceof RpcResponse) {
            byteBuf.writeShort(MessageType.RESPONSE.getCode());
        }

        // 写入序列化方式
        byteBuf.writeShort(serializer.getType());

        // 写入序列化数据长度和内容
        byte[] bytes = serializer.serialize(msg);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

}

