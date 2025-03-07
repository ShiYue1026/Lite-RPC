package com.rpc.client.servicecenter;

import com.rpc.common.message.RpcRequest;
import java.net.InetSocketAddress;

public interface ServiceCenter {

    // 服务发现：根据服务名查找地址
    InetSocketAddress serviceDiscovery(RpcRequest request);

    // 判断是否可重试
    boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature);
}
