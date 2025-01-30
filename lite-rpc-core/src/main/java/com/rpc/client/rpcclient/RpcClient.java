package com.rpc.client.rpcclient;

import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;

public interface RpcClient {

    //定义底层通信的方法
    RpcResponse sendRequest(RpcRequest request);

    // 断开连接
    void stop();
}
