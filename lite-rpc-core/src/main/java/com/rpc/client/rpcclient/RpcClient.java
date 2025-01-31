package com.rpc.client.rpcclient;

import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RpcClient {

    //定义底层通信的方法
    RpcResponse sendRequest(RpcRequest request) throws ExecutionException, InterruptedException;

    // 断开连接
    void stop();
}
