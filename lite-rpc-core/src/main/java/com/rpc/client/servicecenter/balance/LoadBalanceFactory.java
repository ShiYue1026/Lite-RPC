package com.rpc.client.servicecenter.balance;

import com.rpc.client.servicecenter.balance.constant.LoadBalanceType;
import com.rpc.client.servicecenter.balance.impl.ConsistentHashBalancer;
import com.rpc.client.servicecenter.balance.impl.RandomLoadBalancer;
import com.rpc.client.servicecenter.balance.impl.RoundRobinLoadBalancer;
import com.rpc.common.serializer.mySerializer.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LoadBalanceFactory {

    private static final ConcurrentHashMap<String, LoadBalance> loadBalanceMap = new ConcurrentHashMap<>();

    static {
        ServiceLoader.load(LoadBalance.class).forEach(loadBalancer -> {
            loadBalanceMap.put(loadBalancer.getClass().getSimpleName().toLowerCase(), loadBalancer);
        });
    }

    public static LoadBalance getLoadBalance(String type) {
        type = (type + "loadbalancer").toLowerCase();
        log.info("负载均衡类型: {}", type);
        return loadBalanceMap.get(type);
    }
}
