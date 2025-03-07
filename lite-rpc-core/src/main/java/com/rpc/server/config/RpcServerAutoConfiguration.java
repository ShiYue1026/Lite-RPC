package com.rpc.server.config;

import com.rpc.server.initialize.RpcServerInitializer;
import com.rpc.server.provider.ServiceProvider;
import com.rpc.server.rpcserver.RpcServer;
import com.rpc.server.rpcserver.impl.NettyRpcServer;
import com.rpc.server.serviceRegister.ServiceRegister;
import com.rpc.server.serviceRegister.impl.ZKServiceRegister;
import com.rpc.server.tool.SpringContextHolder;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcServerAutoConfiguration {

    @Bean
    ServiceRegister serviceRegister(CuratorFramework client){
        return new ZKServiceRegister(client);
    }

    @Bean
    ServiceProvider serviceProvider(ServiceRegister serviceRegister) {
        return new ServiceProvider(serviceRegister);
    }

    @Bean
    RpcServer RpcServer(ServiceProvider serviceProvider) {
        return new NettyRpcServer(serviceProvider);
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    @Bean
    RpcServerInitializer rpcServerInitializer(ServiceProvider serviceProvider, RpcServer rpcServer) {
        return new RpcServerInitializer(serviceProvider, rpcServer);
    }

}
