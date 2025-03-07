package com.rpc.server.serviceRegister.impl;

import com.rpc.annotation.Retryable;
import com.rpc.server.serviceRegister.ServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class ZKServiceRegister implements ServiceRegister {

    private CuratorFramework client;

    public ZKServiceRegister(CuratorFramework client) {
        this.client = client;
    }

    private static final String RETRY_PATH = "Retry";


    @Override
    public void register(Class<?> clazz, InetSocketAddress serviceAddress) {
        try {
            // serviceName是永久节点，地址是临时节点，服务提供者下线时，不删服务名，只删地址
            String serviceName = clazz.getName();
            log.info(serviceName);
            if(client.checkExists().forPath("/" + serviceName) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
            }
            log.info("注册的服务地址：{}", serviceAddress);
            String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

            // 注册白名单
            List<String> retryableMethods = getRetryableMethod(clazz);
            log.info("可重试的方法: {}", retryableMethods);
            CuratorFramework rootClient = client.usingNamespace(RETRY_PATH);
            for (String retryableMethod : retryableMethods) {
                rootClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/" + getServiceAddress(serviceAddress) + "/" + retryableMethod);
            }

        } catch (Exception e) {
            log.error("此服务已存在");
        }
    }

    // 将InetSocketAddress解析为格式为ip:port的字符串
    private String getServiceAddress(InetSocketAddress serverAddress){
        return serverAddress.getAddress().getHostAddress() + ":" + serverAddress.getPort();
    }

    // 将格式为ip:port的字符串解析为InetSocketAddress
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
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
