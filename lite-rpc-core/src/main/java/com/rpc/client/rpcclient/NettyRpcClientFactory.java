package com.rpc.client.rpcclient;

import com.rpc.client.rpcclient.impl.NettyRpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NettyRpcClientFactory {

    private final ConcurrentHashMap<InetSocketAddress, RpcClient> clientCache = new ConcurrentHashMap<>();

    public RpcClient getClient(InetSocketAddress serviceAddress) {
        return clientCache.computeIfAbsent(serviceAddress, NettyRpcClient::new);
    }
}