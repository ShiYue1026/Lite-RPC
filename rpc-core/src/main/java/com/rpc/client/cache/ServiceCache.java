package com.rpc.client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServiceCache {
    // Key: 服务名  Value: 服务提供者列表
    private static final Map<String, List<String>> cache = new HashMap<>();

    public List<String> getServiceByCache(String serviceName) {
        if(cache.containsKey(serviceName)) {
            log.info("从客户端本地缓存中获取服务地址，无需访问注册中心");
            return cache.get(serviceName);
        }
        return null;
    }

    public void addServiceToCache(String serviceName, String address) {
        List<String> list;
        if(cache.containsKey(serviceName)) {
            list = cache.get(serviceName);
        } else{
            list = new ArrayList<>();
        }
        list.add(address);
        cache.put(serviceName, list);
        log.info("添加新服务到本地缓存: {}-{}", serviceName, address);
    }

    public void updateServiceAddress(String serviceName, String oldAddress, String newAddress) {
        if(!cache.containsKey(serviceName)) {
            throw new RuntimeException("更新缓存节点失败，服务不存在");
        }
        List<String> list = cache.get(serviceName);
        list.remove(oldAddress);
        list.add(newAddress);
        log.info("更新本地缓存: {}的{}更新为{}", serviceName, oldAddress, newAddress);
    }

    public void deleteServiceAddress(String serviceName, String oldAddress) {
        if(!cache.containsKey(serviceName)) {
            throw new RuntimeException("删除缓存节点失败，服务不存在");
        }
        List<String> list = cache.get(serviceName);
        list.remove(oldAddress);
        log.info("从本地缓存删除服务: {}-{}", serviceName, oldAddress);
    }
}
