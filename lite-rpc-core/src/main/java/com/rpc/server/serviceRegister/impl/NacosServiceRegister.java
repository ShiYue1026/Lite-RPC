package com.rpc.server.serviceRegister.impl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.rpc.common.annotation.Retryable;
import com.rpc.server.serviceRegister.ServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class NacosServiceRegister implements ServiceRegister {

    @Override
    public void register(Class<?> clazz, InetSocketAddress serviceAddress) {
        try {
            // serviceName是永久节点，地址是临时节点，服务提供者下线时，不删服务名，只删地址
            String serviceName = clazz.getName();

            // 注册服务和白名单
            Instance instance = new Instance();
            instance.setIp(serviceAddress.getAddress().getHostAddress());
            instance.setPort(serviceAddress.getPort());
            List<String> retryableMethods = getRetryableMethod(clazz);
            log.info("可重试的方法: {}", retryableMethods);
            Map<String, String> retryableMethodMap = new HashMap<>();
            retryableMethodMap.put("Retry",  String.join(",", retryableMethods));
            instance.setMetadata(retryableMethodMap);

            NamingService namingService = NacosFactory.createNamingService("192.168.88.128:8848");
            namingService.registerInstance(serviceName, instance);
            log.info("RPC 服务已注册到 Nacos: {}", serviceName);
        } catch (Exception e) {
            log.error("此服务已存在");
        }
    }

    // 判断一个方法是否加了Retryable注解
    private List<String> getRetryableMethod(Class<?> clazz){
        List<String> retryableMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Retryable.class)) {
                String methodSignature = getMethodSignature(clazz, method);
                retryableMethods.add(methodSignature);
            }
        }
        return retryableMethods;
    }

    private String getMethodSignature(Class<?> clazz, Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getName()).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(",");
            } else{
                sb.append(")");
            }
        }
        return sb.toString();
    }
}
