package com.rpc.client.config;

import com.rpc.client.ApplicationContextProvider;
import com.rpc.client.cache.ServiceCache;
import com.rpc.client.initializer.RpcClientInitializer;
import com.rpc.client.proxy.ClientProxy;
import com.rpc.client.retry.GuavaRetry;
import com.rpc.client.servicecenter.ServiceCenter;
import com.rpc.client.servicecenter.ZKServiceCenter;
import com.rpc.client.servicecenter.ZKWatcher.Watcher;
import com.rpc.client.servicecenter.ZKWatcher.ZKWatcher;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcClientAutoConfiguration {

    @Bean
    ServiceCache serviceCache(){
        return new ServiceCache();
    }

    @Bean
    ServiceCenter serviceCenter(CuratorFramework client, ServiceCache serviceCache){
        return new ZKServiceCenter(client, serviceCache);
    }

    @Bean
    GuavaRetry guavaRetry(MeterRegistry meterRegistry){
        return new GuavaRetry(meterRegistry);
    }

    @Bean
    ClientProxy clientProxy(ServiceCenter serviceCenter, GuavaRetry guavaRetry, MeterRegistry meterRegistry){
        return new ClientProxy(serviceCenter, guavaRetry, meterRegistry);
    }

    @Bean
    Watcher watcher(CuratorFramework client, ServiceCache serviceCache){
        return new ZKWatcher(client, serviceCache);
    }

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    RpcClientInitializer rpcClientInitializer(ClientProxy clientProxy, Watcher watcher){
        return new RpcClientInitializer(clientProxy, watcher);
    }


}
