package com.rpc.client.retry;

import com.github.rholder.retry.*;
import com.rpc.client.rpcclient.RpcClient;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuavaRetry {

    public RpcResponse sendRequestWithRetry(RpcRequest request, RpcClient rpcClient) {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 只要出现异常进行重试
                .retryIfException()
                // Response的响应码为500进行重试
                .retryIfResult(response -> Objects.equals(response.getCode(), 500))
                // 重试等待策略：等待2s后再进行重试
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // 重试停止策略：重试达到3次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("RetryListener: 第{}次调用", attempt.getAttemptNumber());
                    }
                }).build();

        try {
            return retryer.call(() -> rpcClient.sendRequest(request));
        } catch (Exception e){
            e.printStackTrace();
        }
        return RpcResponse.fail();
    }
}
