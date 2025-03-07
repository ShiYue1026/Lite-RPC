package com.rpc.client.rpcclient;

import com.rpc.client.rpcclient.impl.NettyRpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class NettyRpcClientFactory {

    private static final ConcurrentHashMap<InetSocketAddress, RpcClient> clientCache = new ConcurrentHashMap<>();

    public static RpcClient getClient(InetSocketAddress serviceAddress) {
        return clientCache.computeIfAbsent(serviceAddress, NettyRpcClient::new);
    }
}