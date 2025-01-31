package com.rpc.client.initializer;

import com.rpc.annotation.RpcClient;
import com.rpc.client.proxy.ClientProxy;
import com.rpc.client.servicecenter.ZKWatcher.ZKWatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
@RequiredArgsConstructor
public class RpcClientInitializer implements BeanPostProcessor, CommandLineRunner {

    private final ClientProxy clientProxy;

    private final ZKWatcher watcher;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if(field.isAnnotationPresent(RpcClient.class)) {
                field.setAccessible(true);
                Class<?> interfaceClass = field.getType();
                Object proxy = clientProxy.getProxy(interfaceClass);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }

    @Override
    public void run(String... args) throws Exception {
        watcher.watchToUpdate();
    }
}
