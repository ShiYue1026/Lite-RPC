package com.rpc.client.netty.initializer;

import com.rpc.client.ApplicationContextProvider;
import com.rpc.client.netty.handler.NettyClientHandler;
import com.rpc.common.serializer.myCode.MyDecoder;
import com.rpc.common.serializer.myCode.MyEncoder;
import com.rpc.common.serializer.mySerializer.JsonSerializer;
import com.rpc.common.serializer.mySerializer.SerializerFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private static final EventExecutorGroup workerGroup = new DefaultEventExecutorGroup(16); // 16 个线程并发处理 Handler

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 读取序列化方式
        String serializerType = ApplicationContextProvider.getApplicationContext().getEnvironment()
                .getProperty("rpc.serializer", "kryo");

        // 添加心跳检测（空闲 15s 发送心跳）
        pipeline.addLast(new IdleStateHandler(0, 15, 0, TimeUnit.SECONDS));

        // 添加自定义编解码器（在 Netty EventLoop 执行）
        pipeline.addLast(new MyEncoder(SerializerFactory.getSerializer(serializerType)));
        pipeline.addLast(new MyDecoder());

        // 让 NettyClientHandler 在 workerGroup 线程池执行（避免阻塞 EventLoop）
        pipeline.addLast(workerGroup, new NettyClientHandler());
    }
}
