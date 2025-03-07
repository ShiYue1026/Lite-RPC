package com.rpc.client.servicecenter.balance.impl;

import com.rpc.client.servicecenter.balance.LoadBalance;
import com.rpc.common.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class RandomLoadBalancer implements LoadBalance {

    private final Random random = new Random();

    @Override
    public String balance(RpcRequest request, List<String> addressList) {
        int select = random.nextInt(addressList.size());
        log.info("负载均衡: 选择{}服务器", addressList.get(select));
        return addressList.get(select);
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void delNode(String node) {

    }
}
