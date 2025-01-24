package Client.serviceCenter.balance;

import Client.serviceCenter.balance.impl.RoundRobinLoadBalance;

import java.util.HashMap;

public class LoadBalanceFactory {

    private final HashMap<String, LoadBalance> loadBalanceMap = new HashMap<>();

    public LoadBalance getLoadBalance(String type) {
        type = type.toLowerCase();
        return loadBalanceMap.computeIfAbsent(type, key -> createLoadBalance(key));
    }

    private LoadBalance createLoadBalance(String type) {
        switch (type.toLowerCase()) {
            case "roundrobin":
                return new RoundRobinLoadBalance();
            case "random":
                return null;
            case "hash":
                return null;
            default:
                throw new IllegalArgumentException("Unknown load balance type: " + type);
        }
    }
}
