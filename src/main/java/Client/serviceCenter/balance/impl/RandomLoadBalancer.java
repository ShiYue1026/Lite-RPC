package Client.serviceCenter.balance.impl;

import Client.serviceCenter.balance.LoadBalance;
import common.Message.RpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer implements LoadBalance {

    private final Random random = new Random();

    @Override
    public String balance(RpcRequest request, List<String> addressList) {
        int select = random.nextInt(addressList.size());
        System.out.println("负载均衡: 选择" + addressList.get(select) + "服务器");
        return addressList.get(select);
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void delNode(String node) {

    }
}
