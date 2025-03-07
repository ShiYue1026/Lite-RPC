package com.rpc.client.servicecenter.balance;


import com.rpc.common.message.RpcRequest;

import java.util.List;

public interface LoadBalance {

    String balance(RpcRequest request, List<String> addressList);

    void addNode(String node);

    void delNode(String node);
}
