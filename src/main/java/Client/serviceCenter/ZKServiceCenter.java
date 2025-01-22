package Client.serviceCenter;

import Client.cache.ServiceCache;
import Client.serviceCenter.ZKWatcher.ZKWatcher;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

public class ZKServiceCenter implements ServiceCenter {

    private CuratorFramework client;

    private static final String ROOT_PATH = "MyRPC";

    private static final String IP_PORT = "127.0.0.1:2181";

    private ServiceCache serviceCache;

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
        System.out.println("zookeeper连接成功");

        // 监听注册中心节点变化
        serviceCache = new ServiceCache();
        ZKWatcher watcher = new ZKWatcher(client, serviceCache);
        watcher.watchToUpdate();
    }

    // 服务发现
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            List<String> services = serviceCache.getServiceByCache(serviceName);
            if(services == null){  // 本地缓存没查到
                services = client.getChildren().forPath("/" + serviceName);
            }
            // 默认使用第一个服务，后面再加负载均衡
            String service = services.get(0);
            return parseAddress(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
