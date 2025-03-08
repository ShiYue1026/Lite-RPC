package com.rpc.client.servicecenter;

import com.rpc.client.cache.ServiceCache;
import com.rpc.client.servicecenter.balance.LoadBalanceFactory;
import com.rpc.common.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
public class ZKServiceCenter implements ServiceCenter {

    private static final String RETRY_PATH = "Retry";  // 存放可以进行重试的方法

    private CuratorFramework client;

    private ServiceCache serviceCache;

    public ZKServiceCenter(CuratorFramework client, ServiceCache serviceCache) {
        this.client = client;
        this.serviceCache = serviceCache;
    }

    @Value("${rpc.loadBalance:random}")
    private String loadBalanceType;

    // 服务发现
    @Override
    public InetSocketAddress serviceDiscovery(RpcRequest request) {
        try {
            String serviceName = request.getInterfaceName();
            List<String> services = serviceCache.getServiceByCache(serviceName);
            if(services == null){  // 本地缓存没查到
                services = client.getChildren().forPath("/" + serviceName);
            }
            // 负载均衡
            services = new CopyOnWriteArrayList<>(services);
            String service = LoadBalanceFactory.getLoadBalance(loadBalanceType).balance(request, services);
            return parseAddress(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature) {
        try {
            CuratorFramework rootClient = client.usingNamespace(RETRY_PATH);
            List<String> retryableMethods = rootClient.getChildren().forPath("/" + getServiceAddress(serviceAddress));
            for(String s : retryableMethods){
                if(s.equals(methodSignature)){
                    log.info("方法{}在白名单上，可进行重试", methodSignature);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
}
