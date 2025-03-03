package com.rpc.client.circuitbreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CircuitBreaker {

    private volatile CircuitBreakerStatus status = CircuitBreakerStatus.CLOSED;

    private volatile AtomicInteger failureCount = new AtomicInteger(0);
    private volatile AtomicInteger successCount = new AtomicInteger(0);
    private volatile AtomicInteger requestCount = new AtomicInteger(0);

    // 失败次数阈值
    private final int failureThreshold;

    // HALF_OPEN -> CLOSED 的成功次数比例
    private final double halfOpenSuccessRate;

    // 重试时间
    private final long retryTimePeriod;

    // 上一次失败时间
    private volatile long lastFailureTime = 0;

    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.retryTimePeriod = retryTimePeriod;
    }

    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        switch (status) {
            case OPEN:
                if (currentTime - lastFailureTime > retryTimePeriod) {  // 进入HALF_OPEN状态重试
                    status = CircuitBreakerStatus.HALF_OPEN;
                    resetCounts();
                    return true;
                }
                return false;
            case HALF_OPEN:
                requestCount.incrementAndGet();
                return true;
            case CLOSED:
                return true;
            default:
                throw new IllegalStateException("Unexpected value: " + status);
        }
    }

    // 记录成功次数
    public synchronized void recordSuccess() {
        if (status == CircuitBreakerStatus.HALF_OPEN) {
            successCount.incrementAndGet();
            if(successCount.get() >= halfOpenSuccessRate * requestCount.get()) {
                status = CircuitBreakerStatus.CLOSED;
                resetCounts();
            }
        } else {
            resetCounts();
        }
    }

    // 记录失败次数
    public synchronized void recordFailure() {
        failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        if(status == CircuitBreakerStatus.CLOSED && failureCount.get() >= failureThreshold) {
            status = CircuitBreakerStatus.OPEN;
            resetCounts();
        }

        // 在 HALF_OPEN 状态下失败过多时，回到 OPEN 状态
        if (status == CircuitBreakerStatus.HALF_OPEN) {
            int failureLimit = (int) Math.ceil((1 - halfOpenSuccessRate) * requestCount.get());
            if (failureCount.get() >= failureLimit) {
                status = CircuitBreakerStatus.OPEN;
                lastFailureTime = System.currentTimeMillis();  // 重新计时
                resetCounts();
            }
        }
    }

    private void resetCounts() {
        failureCount = new AtomicInteger(0);
        successCount = new AtomicInteger(0);
        requestCount = new AtomicInteger(0);
    }
}
