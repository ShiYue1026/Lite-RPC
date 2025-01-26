package Client.proxy;

import Client.circuitBreaker.CircuitBreaker;
import Client.circuitBreaker.CircuitBreakerFactory;
import Client.retry.GuavaRetry;
import Client.rpcClient.RpcClient;
import Client.rpcClient.impl.NettyRpcClient;
import Client.serviceCenter.ServiceCenter;
import Client.serviceCenter.ZKServiceCenter;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

public class ClientProxy implements InvocationHandler {

    public ServiceCenter serviceCenter;

    public RpcClient rpcClient;

    private CircuitBreakerFactory circuitBreakerFactory;

    public ClientProxy(){
        serviceCenter = new ZKServiceCenter();
        circuitBreakerFactory = new CircuitBreakerFactory();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //构建request
        RpcRequest request=RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();

        // 熔断机制
        CircuitBreaker circuitBreaker = circuitBreakerFactory.getCircuitBreaker(getMethodSignature(request.getInterfaceName(), method));
        if(!circuitBreaker.allowRequest()){
            System.out.println("熔断器生效，当前请求被熔断");
            return null;
        }

        // 重试机制
        String methodSignature = getMethodSignature(request.getInterfaceName(), method);
        System.out.println("方法签名: " + methodSignature);
        RpcResponse response = null;
        InetSocketAddress serviceAddress = serviceCenter.serviceDiscovery(request);
        rpcClient = new NettyRpcClient(serviceAddress);
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

    public <T>T getProxy(Class<T> clazz){
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }

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

}
