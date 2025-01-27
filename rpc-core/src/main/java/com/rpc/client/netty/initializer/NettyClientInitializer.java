package com.rpc.client.netty.initializer;

import com.rpc.client.netty.handler.NettyClientHandler;
import com.rpc.serializer.myCode.MyDecoder;
import com.rpc.serializer.myCode.MyEncoder;
import com.rpc.serializer.mySerializer.JsonSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

//        // 消息格式: [长度] + [消息体]，解决粘包问题
//        pipeline.addLast(
//                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//
//        // 计算当前待发送字节流的长度，写入到前4个字节中
//        pipeline.addLast(new LengthFieldPrepender(4));
//
//        // 使用Java序列化方式
//        pipeline.addLast(new ObjectEncoder());

//        // 解码器
//        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
//            @Override
//            public Class<?> resolve(String className) throws ClassNotFoundException {
//                return Class.forName(className);
//            }
//        }));

        pipeline.addLast(new MyEncoder(new JsonSerializer()));

        pipeline.addLast(new MyDecoder());

        pipeline.addLast(new NettyClientHandler());
    }
}
