package Client.serviceCenter.balance;

import Client.serviceCenter.balance.constant.LoadBalanceType;
import Client.serviceCenter.balance.impl.ConsistentHashBalancer;
import Client.serviceCenter.balance.impl.RandomLoadBalancer;
import Client.serviceCenter.balance.impl.RoundRobinLoadBalancer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class LoadBalanceFactory {

    private final ConcurrentHashMap<String, LoadBalance> loadBalanceMap = new ConcurrentHashMap<>();

    public LoadBalance getLoadBalance(String type) {
        type = type.toLowerCase();
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
