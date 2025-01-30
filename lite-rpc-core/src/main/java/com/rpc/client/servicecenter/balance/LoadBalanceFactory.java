package com.rpc.client.servicecenter.balance;

import com.rpc.client.servicecenter.balance.constant.LoadBalanceType;
import com.rpc.client.servicecenter.balance.impl.ConsistentHashBalancer;
import com.rpc.client.servicecenter.balance.impl.RandomLoadBalancer;
import com.rpc.client.servicecenter.balance.impl.RoundRobinLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LoadBalanceFactory {

    private final ConcurrentHashMap<String, LoadBalance> loadBalanceMap = new ConcurrentHashMap<>();

    public LoadBalance getLoadBalance(String type) {
        type = type.toLowerCase();
        log.info("负载均衡类型: {}", type);
        return loadBalanceMap.computeIfAbsent(type, key -> createLoadBalance(key));
    }

    private LoadBalance createLoadBalance(String type) {
        switch (type.toLowerCase()) {
            case LoadBalanceType.ROUND_ROBIN:
                return new RoundRobinLoadBalancer();
            case LoadBalanceType.RANDOM:
                return new RandomLoadBalancer();
            case LoadBalanceType.CONSISTENT_HASH:
                return new ConsistentHashBalancer();
            default:
                throw new IllegalArgumentException("Unknown load balance type: " + type);
        }
    }
}
