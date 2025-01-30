package com.rpc.client.rpcclient;

import com.rpc.client.rpcclient.impl.NettyRpcClient;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class NettyRpcClientFactory {

    public RpcClient createClient(InetSocketAddress serviceAddress) {
        return new NettyRpcClient(serviceAddress);
    }
}