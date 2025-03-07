package com.rpc.client.servicecenter;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.rpc.client.cache.ServiceCache;
import com.rpc.client.servicecenter.balance.LoadBalanceFactory;
import com.rpc.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class NacosServiceCenter implements ServiceCenter {

    private ServiceCache serviceCache;

    public NacosServiceCenter(ServiceCache serviceCache) {
        this.serviceCache = serviceCache;
    }

    @Value("${rpc.loadBalance}")
    private String loadBalanceType;

    // 服务发现
    @Override
    public InetSocketAddress serviceDiscovery(RpcRequest request) {
        try {
            String serviceName = request.getInterfaceName();
            List<String> services = serviceCache.getServiceByCache(serviceName);
            if(services == null){  // 本地缓存没查到
                services = new CopyOnWriteArrayList<>();
                NamingService namingService = NacosFactory.createNamingService("192.168.88.128:8848");
                List<Instance> instances = namingService.getAllInstances(serviceName);
                for(Instance instance : instances){
                    services.add(instance.getIp() + ":" + instance.getPort());
                }
            }
            // 负载均衡
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
            String serviceName = methodSignature.split("#")[0];
            NamingService namingService = NacosFactory.createNamingService("192.168.88.128:8848");
            List<Instance> instances = namingService.getAllInstances(serviceName);
            String retryableMethodstr = "";
            for (Instance instance : instances) {
                if (instance.getIp().equals(serviceAddress.getAddress().getHostAddress()) && instance.getPort() == serviceAddress.getPort()) {
                    retryableMethodstr = instance.getMetadata().get("Retry");
                }
            }
            List<String> retryableMethods = List.of(retryableMethodstr.split(","));
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
