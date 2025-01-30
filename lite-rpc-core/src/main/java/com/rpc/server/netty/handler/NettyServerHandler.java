package com.rpc.server.netty.handler;

import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import com.rpc.server.provider.ServiceProvider;
import com.rpc.server.ratelimit.RateLimit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ServiceProvider serviceProvider;

    public NettyServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        // 接收request，读取并调用服务
        RpcResponse response = getResponse(request);
        ctx.writeAndFlush(response);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest request) {
        // 得到要请求的接口名
        String interfaceName = request.getInterfaceName();

        // 根据接口名进行限流
        RateLimit rateLimit = serviceProvider.getRateLimit(interfaceName);
        if(!rateLimit.getToken()){
            log.info("接口繁忙，请稍后再试");
            return RpcResponse.fail();
        }

        // 得到服务端相应的接口实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用方法
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsType());
            Object invoke = method.invoke(service, request.getParams());
            return RpcResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            log.error("方法执行错误");
            return RpcResponse.fail();
        }
    }
}
