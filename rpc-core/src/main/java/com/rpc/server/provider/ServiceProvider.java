package com.rpc.server.provider;


import com.rpc.server.ratelimit.RateLimit;
import com.rpc.server.ratelimit.RateLimitFactory;
import com.rpc.server.ratelimit.constant.RateLimitType;
import com.rpc.server.serviceRegister.ServiceRegister;
import com.rpc.server.serviceRegister.impl.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {

    private Map<String, Object> interfaceProvider;

    private String host;

    private int port;

    private ServiceRegister serviceRegister;

    private RateLimitFactory rateLimitFactory;  // 接口限流器

    public ServiceProvider(String host, int port) {
        this.interfaceProvider = new HashMap<>();
        this.host = host;
        this.port = port;
        this.serviceRegister = new ZKServiceRegister();
        this.rateLimitFactory = new RateLimitFactory();
    }

    public void provideServiceInterface(Object service) {
        String serviceName = service.getClass().getName();
        Class<?>[] interfaces = service.getClass().getInterfaces();

        for (Class<?> clazz: interfaces){
            // 本机的映射表
            interfaceProvider.put(clazz.getName(), service);

            // 向注册中心注册服务
            serviceRegister.register(clazz, new InetSocketAddress(host, port));
        }
    }

    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }

    public RateLimit getRateLimit(String interfaceName){
        return rateLimitFactory.getRateLimit(interfaceName, RateLimitType.TOKEN_BUCKET);
    }

}
