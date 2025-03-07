package com.rpc.server.initialize;

import com.rpc.annotation.RpcService;
import com.rpc.server.provider.ServiceProvider;
import com.rpc.server.rpcserver.RpcServer;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;



@Slf4j
@RequiredArgsConstructor
public class RpcServerInitializer implements CommandLineRunner, BeanPostProcessor {

    private final ServiceProvider serviceProvider;

    private final RpcServer nettyRpcServer;

    @Value("${rpc.port}")
    private int port;

    @Override
    public void run(String... args) {
        nettyRpcServer.start(port);
    }

    /**
        在Bean初始化完成之后，获取@RpcService标记的服务类注册到注册中心
    **/
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            for (Class<?> interfaceClass : interfaces) {
                log.info("注册接口名: {}", interfaceClass.getName());

            }

            try {
                Object instance = bean.getClass().getDeclaredConstructor().newInstance();  // 通过反射创建实例
                serviceProvider.provideServiceInterface(instance);
            } catch (Exception e) {
                log.error("RPC服务初始化错误");
            }
        }
        return bean;
    }
}
