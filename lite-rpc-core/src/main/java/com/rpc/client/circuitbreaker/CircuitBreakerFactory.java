package com.rpc.client.circuitbreaker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CircuitBreakerFactory {

    private static final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public static synchronized CircuitBreaker getCircuitBreaker(String methodSignature) {
        CircuitBreaker circuitBreaker;
        if(circuitBreakers.containsKey(methodSignature)) {
            return circuitBreakers.get(methodSignature);
        }
        log.info("方法{}创建一个新的熔断器", methodSignature);
        circuitBreaker = new CircuitBreaker(5, 0.6, 5000);
        circuitBreakers.put(methodSignature, circuitBreaker);
        return circuitBreaker;
    }


}
