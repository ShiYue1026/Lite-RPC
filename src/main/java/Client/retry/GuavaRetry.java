package Client.retry;

import Client.rpcClient.RpcClient;
import com.github.rholder.retry.*;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
                        System.out.println("RetryListener: 第" + attempt.getAttemptNumber() + "次调用");
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
