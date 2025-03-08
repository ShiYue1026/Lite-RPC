package com.rpc.client.rpcclient;

import com.rpc.common.message.RpcRequest;
import com.rpc.common.message.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RpcClient {

    //定义底层通信的方法
    CompletableFuture<RpcResponse> sendRequest(RpcRequest request) throws ExecutionException, InterruptedException;

    // 断开连接
    void stop();
}
