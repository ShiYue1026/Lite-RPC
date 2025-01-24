package Client.serviceCenter.balance.impl;

import Client.serviceCenter.balance.LoadBalance;
import common.Message.RpcRequest;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalance {

    private final ConcurrentMap<String, AtomicInteger> sequences = new ConcurrentHashMap<>();

    @Override
    public String balance(RpcRequest request, List<String> addressList) {
        try {
            String key = getServiceKey(request); // 每个方法级自己轮询，互不影响
            AtomicInteger sequence = sequences.computeIfAbsent(key, k -> new AtomicInteger(0));
            int length = addressList.size();
            int select = sequence.get();
            sequence.getAndIncrement();
            System.out.println("负载均衡: 选择" + addressList.get(select % length) + "服务器");
            return addressList.get(select % length);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void delNode(String node) {

    }

    private String getServiceKey(RpcRequest request) {
        return request.getInterfaceName() + "#" +
                request.getMethodName();
    }
}