package com.rpc.client.servicecenter.balance.impl;

import com.rpc.client.servicecenter.balance.LoadBalance;
import com.rpc.common.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
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
            log.info("负载均衡: 选择{}服务器", addressList.get(select % length));
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