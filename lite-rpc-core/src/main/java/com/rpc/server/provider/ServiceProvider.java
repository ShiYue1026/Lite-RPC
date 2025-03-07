package com.rpc.server.provider;


import com.rpc.server.ratelimit.RateLimit;
import com.rpc.server.ratelimit.RateLimitFactory;
import com.rpc.server.ratelimit.constant.RateLimitType;
import com.rpc.server.serviceRegister.ServiceRegister;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {

    private final Map<String, Object> interfaceProvider = new HashMap<>();

    private final ServiceRegister serviceRegister;

    @Getter
    @Value("${rpc.port}")
    private int port;

    public ServiceProvider(ServiceRegister serviceRegister) {
        this.serviceRegister = serviceRegister;
    }

    public void provideServiceInterface(Object service) {
        try {
            String serviceName = service.getClass().getName();
            Class<?>[] interfaces = service.getClass().getInterfaces();
            String host = InetAddress.getLocalHost().getHostAddress();

            for (Class<?> clazz: interfaces){
                // 本机的映射表
                interfaceProvider.put(clazz.getName(), service);

                // 向注册中心注册服务
                serviceRegister.register(clazz, new InetSocketAddress(host, port));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }

    public RateLimit getRateLimit(String interfaceName){
        return RateLimitFactory.getRateLimit(interfaceName, RateLimitType.TOKEN_BUCKET);
    }

}
