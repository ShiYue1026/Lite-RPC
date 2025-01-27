package com.rpc.client.servicecenter.balance.impl;

import com.rpc.client.servicecenter.balance.LoadBalance;
import com.rpc.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Slf4j
public class ConsistentHashBalancer implements LoadBalance {

    private static final int VIRTUAL_NUM = 64;

    private final TreeMap<Integer, String> shards = new TreeMap<>();

    private List<String> realNodes = new ArrayList<>();

    private void init(List<String> addressList) {
        for (String address : addressList) {
            realNodes.add(address);
            for(int i=0; i<VIRTUAL_NUM; i++) {
                String virtualNode = address + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.put(hash, virtualNode);
            }
        }
    }

    @Override
    public String balance(RpcRequest request, List<String> addressList) {
        init(addressList);
        String s = request.getInterfaceName() + "#" + request.getMethodName();
        int hash = getHash(s);
        SortedMap<Integer, String> subMap = shards.tailMap(hash);
        Integer key;
        if(subMap.isEmpty()) {
            key = shards.firstKey();
        } else{
            key = subMap.firstKey();
        }
        String virtualNode = shards.get(key);
        log.info("负载均衡: 选择{}服务器", virtualNode.substring(0, virtualNode.indexOf("&&VN")));
        return virtualNode.substring(0, virtualNode.indexOf("&&VN"));
    }

    @Override
    public void addNode(String node) {
        realNodes.add(node);
        for(int i=0; i<VIRTUAL_NUM; i++) {
            String virtualNode = realNodes.get(i) + "&&VN" + i;
            int hash = getHash(virtualNode);
            shards.put(hash, virtualNode);
        }
    }

    @Override
    public  void delNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
            }
        }
    }

    /**
     * FNV1_32_HASH算法
     */
    private static int getHash(String s) {
        final int prime = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < s.length(); i++) {
            hash = (hash ^ s.charAt(i)) * prime;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash < 0 ? Math.abs(hash) : hash;
    }
}
