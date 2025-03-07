package com.rpc.client.retry;

import com.github.rholder.retry.*;
import com.rpc.client.rpcclient.RpcClient;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuavaRetry {

    private MeterRegistry meterRegistry;

    public GuavaRetry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

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
                        Counter requestCounter = meterRegistry.counter("rpc_requests_total");
                        requestCounter.increment();
                        log.info("RetryListener: 第{}次调用", attempt.getAttemptNumber());
                        if((int) attempt.getAttemptNumber() != 1){
                            Counter retryCounter = meterRegistry.counter("rpc_retries_total");
                            retryCounter.increment();
                        }
                    }
                }).build();

        try {
            return retryer.call(() -> rpcClient.sendRequest(request));
        } catch (Exception e){
            e.printStackTrace();
        }
        return RpcResponse.fail(request.getRequestId());
    }
}
