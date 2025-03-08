package com.rpc.server.config;

import com.rpc.common.annotation.RpcService;
import com.rpc.server.initialize.RpcServerInitializer;
import com.rpc.server.provider.ServiceProvider;
import com.rpc.server.rpcserver.RpcServer;
import com.rpc.server.rpcserver.impl.NettyRpcServer;
import com.rpc.server.serviceRegister.ServiceRegister;
import com.rpc.server.serviceRegister.impl.ZKServiceRegister;
import com.rpc.server.tool.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnBean(annotation = RpcService.class)
public class RpcServerAutoConfiguration {

    @Bean
    ServiceRegister serviceRegister(CuratorFramework client){
        log.info("自动配置: ServiceRegister");
        return new ZKServiceRegister(client);
    }

    @Bean
    ServiceProvider serviceProvider(ServiceRegister serviceRegister) {
        log.info("自动配置: ServiceProvider");
        return new ServiceProvider(serviceRegister);
    }

    @Bean
    RpcServer RpcServer(ServiceProvider serviceProvider) {
        log.info("自动配置: RpcServer");
        return new NettyRpcServer(serviceProvider);
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    @Bean
    RpcServerInitializer rpcServerInitializer(ServiceProvider serviceProvider, RpcServer rpcServer) {
        log.info("自动配置: RpcServerInitializer");
        return new RpcServerInitializer(serviceProvider, rpcServer);
    }

}
