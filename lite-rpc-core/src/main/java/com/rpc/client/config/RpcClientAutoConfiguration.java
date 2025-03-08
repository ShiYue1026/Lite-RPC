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
import com.rpc.common.annotation.RpcService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnMissingBean(annotation = RpcService.class)
public class RpcClientAutoConfiguration {

    @Bean
    ServiceCache serviceCache(){
        log.info("自动配置: ServiceCache");
        return new ServiceCache();
    }

    @Bean
    ServiceCenter serviceCenter(CuratorFramework client, ServiceCache serviceCache){
        log.info("自动配置: ServiceCenter");
        return new ZKServiceCenter(client, serviceCache);
    }

    @Bean
    GuavaRetry guavaRetry(MeterRegistry meterRegistry){
        log.info("自动配置: GuavaRetry");
        return new GuavaRetry(meterRegistry);
    }

    @Bean
    ClientProxy clientProxy(ServiceCenter serviceCenter, GuavaRetry guavaRetry, MeterRegistry meterRegistry){
        log.info("自动配置: ClientProxy");
        return new ClientProxy(serviceCenter, guavaRetry, meterRegistry);
    }

    @Bean
    Watcher watcher(CuratorFramework client, ServiceCache serviceCache){
        log.info("自动配置: Watcher");
        return new ZKWatcher(client, serviceCache);
    }

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    RpcClientInitializer rpcClientInitializer(ClientProxy clientProxy, Watcher watcher){
        log.info("自动配置: RpcClientInitializer");
        return new RpcClientInitializer(clientProxy, watcher);
    }


}
