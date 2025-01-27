package com.rpc.client.servicecenter;

import com.rpc.client.cache.ServiceCache;
import com.rpc.client.servicecenter.ZKWatcher.ZKWatcher;
import com.rpc.client.servicecenter.balance.LoadBalanceFactory;
import com.rpc.client.servicecenter.balance.constant.LoadBalanceType;
import com.rpc.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;


@Slf4j
public class ZKServiceCenter implements ServiceCenter {

    private CuratorFramework client;

    private static final String ROOT_PATH = "MyRPC";

    private static final String RETRY_PATH = "Retry";  // 存放可以进行重试的方法

    private static final String IP_PORT = "127.0.0.1:2181";

    private ServiceCache serviceCache;

    private LoadBalanceFactory loadBalanceFactory;

    // 初始化zookeeper客户端，并与zookeeper服务端建立连接
    public ZKServiceCenter() {
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(IP_PORT)
                .sessionTimeoutMs(40000)
                .retryPolicy(policy)
                .namespace(ROOT_PATH)
                .build();

        client.start();
        log.info("zookeeper连接成功");

        // 监听注册中心节点变化
        serviceCache = new ServiceCache();
        ZKWatcher watcher = new ZKWatcher(client, serviceCache);
        watcher.watchToUpdate();

        loadBalanceFactory = new LoadBalanceFactory();
    }

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
            String service = loadBalanceFactory.getLoadBalance(LoadBalanceType.ROUND_ROBIN).balance(request, services);
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
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }

    // 将格式为ip:port的字符串解析为InetSocketAddress
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
