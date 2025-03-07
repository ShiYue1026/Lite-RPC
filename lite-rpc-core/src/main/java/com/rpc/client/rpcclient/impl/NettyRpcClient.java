package com.rpc.client.rpcclient.impl;

import com.rpc.client.netty.initializer.NettyClientInitializer;
import com.rpc.client.rpcclient.RpcClient;
import com.rpc.common.message.RpcRequest;
import com.rpc.common.message.RpcResponse;
import com.rpc.common.util.PendingProcessedMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.prometheus.metrics.core.metrics.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


@Slf4j
public class NettyRpcClient implements RpcClient {

    private static final Bootstrap bootstrap;

    private static final EventLoopGroup eventLoopGroup;

    private final InetSocketAddress address;

    private volatile Channel channel;


    public NettyRpcClient(InetSocketAddress serviceAddress) {
        this.address = serviceAddress;
    }

    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    private synchronized void connect() {
        if (channel == null || !channel.isActive()) {
            try {
                ChannelFuture future = bootstrap.connect(address).sync();
                channel = future.channel();
            } catch (InterruptedException e) {
                throw new RuntimeException("无法连接 RPC 服务器", e);
            }
        }
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) throws ExecutionException, InterruptedException {
        // 从注册中心获取服务地址
        connect();

        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        PendingProcessedMap.put(request.getRequestId(), future);

        channel.writeAndFlush(request);

        return future.get();
    }


    @Override
    public void stop() {
        if (channel != null) {
            channel.close();
        }
        eventLoopGroup.shutdownGracefully();
    }

}