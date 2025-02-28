package com.rpc.client.proxy;

import com.rpc.annotation.FallBack;
import com.rpc.client.circuitbreaker.CircuitBreaker;
import com.rpc.client.circuitbreaker.CircuitBreakerFactory;
import com.rpc.client.retry.GuavaRetry;
import com.rpc.client.rpcclient.NettyRpcClientFactory;
import com.rpc.client.rpcclient.RpcClient;
import com.rpc.client.rpcclient.impl.NettyRpcClient;
import com.rpc.client.servicecenter.ServiceCenter;
import com.rpc.client.servicecenter.ZKServiceCenter;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import com.rpc.util.PendingProcessedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;


@Slf4j
@Component
public class ClientProxy implements InvocationHandler {

    @Autowired
    private ServiceCenter serviceCenter;

    @Autowired
    private NettyRpcClientFactory clientFactory;

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    private Class<?> fallbackClass;

    public ClientProxy(){}

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //构建request
        RpcRequest request=RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();

        // 熔断机制
        CircuitBreaker circuitBreaker = circuitBreakerFactory.getCircuitBreaker(getMethodSignature(request.getInterfaceName(), method));

        if(!circuitBreaker.allowRequest()){
            log.info("熔断器生效，当前请求被熔断");

            // 进行fallback处理
            if(fallbackClass != null){
                return executeFallback(fallbackClass, method, args);  // 执行fallback方法并直接返回
            }

            throw new RuntimeException("熔断器生效且没有指定 fallback 方法");

        }

        // 重试机制
        String methodSignature = getMethodSignature(request.getInterfaceName(), method);
        log.info("方法签名: {}", methodSignature);
        RpcResponse response = null;
        InetSocketAddress serviceAddress = serviceCenter.serviceDiscovery(request);
        RpcClient rpcClient = clientFactory.getClient(serviceAddress);
        if(serviceCenter.checkRetry(serviceAddress, methodSignature)){
            response = new GuavaRetry().sendRequestWithRetry(request, rpcClient);
        } else {
            response= rpcClient.sendRequest(request);
        }

        // 统计请求是否成功，上报给熔断器
        if (response.getCode() == 200){
            circuitBreaker.recordSuccess();
        }
        if (response.getCode() == 500){
            circuitBreaker.recordFailure();
        }

        // 返回响应数据
        return response.getData();
    }

//    public <T>T getProxy(Class<T> clazz, Class<T> fallbackClazz){
//        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ClientProxy(fallbackClazz));
//        return (T)o;
//    }

    // 根据接口名字和方法获取方法签名
    private String getMethodSignature(String interfaceName, Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(interfaceName).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(",");
            } else{
                sb.append(")");
            }
        }
        return sb.toString();
    }

    // 执行fallback方法
    private Object executeFallback(Class<?> fallbackClass, Method method, Object[] args) {
        try{
            Method fallbackMethod = fallbackClass.getMethod(method.getName(), method.getParameterTypes());
            Object fallbackInstance = fallbackClass.getDeclaredConstructor().newInstance();
            return fallbackMethod.invoke(fallbackInstance, args);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public Object getProxy(Class<?> interfaceClass, Class<?> fallbackClass) {
        this.fallbackClass = fallbackClass;
        Object o = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, this);
        return o;
    }
}
