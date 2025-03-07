package com.rpc.common.util;

import com.rpc.common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class PendingProcessedMap {
    public static final ConcurrentHashMap<String, CompletableFuture<RpcResponse>> pendingRequests = new ConcurrentHashMap<>();

    public static void put(String requestId, CompletableFuture<RpcResponse> future) {
        pendingRequests.put(requestId, future);
    }

    public static void receiveResponse(RpcResponse response) {
        CompletableFuture<RpcResponse> future = pendingRequests.remove(response.getResponseId());
        if (future != null) {
            future.complete(response);
        } else {
            log.warn("未找到请求 ID，可能是超时或已处理完毕: {}", response.getResponseId());
        }
    }
}
