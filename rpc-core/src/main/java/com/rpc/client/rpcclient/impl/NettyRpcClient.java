package com.rpc.client.rpcclient.impl;

import com.rpc.client.netty.initializer.NettyClientInitializer;
import com.rpc.client.rpcclient.RpcClient;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@Slf4j
public class NettyRpcClient implements RpcClient {

    private static final Bootstrap bootstrap;

    private static final EventLoopGroup eventLoopGroup;

    private final InetSocketAddress address;

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

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        // 从注册中心获取服务地址
        String host = address.getHostName();
        int port = address.getPort();
        try{
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();
            channel.writeAndFlush(request);
            channel.closeFuture().sync();
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
            RpcResponse response = channel.attr(key).get();

            log.info(String.valueOf(request));
            log.info(String.valueOf(response));
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void stop() {
        eventLoopGroup.shutdownGracefully();
    }

}
