package com.rpc.client.servicecenter.ZKWatcher;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import com.rpc.client.cache.ServiceCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rpc.registry", havingValue = "nacos")
public class NacosWatcher implements Watcher {

    private final ServiceCache serviceCache; // 本地缓存

    public void watchToUpdate() {
        try {
            NamingService namingService = NacosFactory.createNamingService("192.168.88.128:8848");

            for (String serviceName : serviceCache.getCache().keySet()) {
                // 监听 Nacos 服务变更
                namingService.subscribe(serviceName, event -> {
                    if (event instanceof NamingEvent) {
                        List<Instance> instances = ((NamingEvent) event).getInstances();
                        List<String> services = new ArrayList<>();
                        for(Instance instance : instances){
                           services.add(instance.getIp() + ":" + instance.getPort());
                        }
                        serviceCache.getCache().clear();
                        serviceCache.getCache().put(serviceName, services);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Nacos 监听失败", e);
        }
    }
}
