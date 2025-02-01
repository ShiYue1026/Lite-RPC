package com.rpc.server.netty.initializer;

import com.rpc.serializer.myCode.MyDecoder;
import com.rpc.serializer.myCode.MyEncoder;
import com.rpc.serializer.mySerializer.JsonSerializer;
import com.rpc.server.netty.handler.NettyServerHandler;
import com.rpc.server.provider.ServiceProvider;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;


@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private ServiceProvider serviceProvider;

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
//
//        // 解码器
//        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
//            @Override
//            public Class<?> resolve(String className) throws ClassNotFoundException {
//                return Class.forName(className);
//            }
//        }));

        pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));

        pipeline.addLast(new MyEncoder(new JsonSerializer()));

        pipeline.addLast(new MyDecoder());

        pipeline.addLast(new NettyServerHandler(serviceProvider));
    }
}
