package com.rpc.server.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZKconfig {

    private static final String IP_PORT = "127.0.0.1:2181"; // Zookeeper 服务器地址
    private static final String ROOT_PATH = "MyRPC";
    private static final int SESSION_TIMEOUT = 40000;
    private static final int BASE_SLEEP_TIME_MS = 1000; // 初始重试等待时间
    private static final int MAX_RETRIES = 3; // 最大重试次数

    @Bean
    public CuratorFramework curatorFramework() {
        RetryPolicy policy = new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES);
        // 创建 CuratorFramework 实例
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(IP_PORT)
                .sessionTimeoutMs(SESSION_TIMEOUT)
                .retryPolicy(policy)
                .namespace(ROOT_PATH)
                .build();

        // 启动客户端
        client.start();
        System.out.println("服务端Zookeeper 连接成功: " + IP_PORT);

        // 关闭 Hook，确保应用关闭时释放资源
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));

        return client;
    }
}
