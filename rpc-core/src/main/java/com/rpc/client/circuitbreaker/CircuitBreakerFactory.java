package com.rpc.client.circuitbreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CircuitBreakerFactory {

    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public synchronized CircuitBreaker getCircuitBreaker(String methodSignature) {
        CircuitBreaker circuitBreaker;
        if(circuitBreakers.containsKey(methodSignature)) {
            return circuitBreakers.get(methodSignature);
        }
        log.info("方法{}创建一个新的熔断器", methodSignature);
        circuitBreaker = new CircuitBreaker(1, 0.5, 10000);
        circuitBreakers.put(methodSignature, circuitBreaker);
        return circuitBreaker;
    }


}
