package com.rpc.server.netty.handler;

import com.rpc.message.RpcHeartBeat;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import com.rpc.server.provider.ServiceProvider;
import com.rpc.server.ratelimit.RateLimit;
import com.rpc.server.tool.SpringContextHolder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Object>{

    private final MeterRegistry meterRegistry;

    private final ServiceProvider serviceProvider;

    private static final int MAX_IDLE_TIME = 60;  // 一分钟内没有非心跳请求就断开连接

    private int idleTime = 0;

    public NettyServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        this.meterRegistry = SpringContextHolder.getBean(MeterRegistry.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {
        if (object instanceof RpcRequest) {
            idleTime = 0;
            RpcRequest request = (RpcRequest) object;
            // 接收request，读取并调用服务
            RpcResponse response = getResponse(request);
            ctx.writeAndFlush(response);
        } else if(object instanceof RpcHeartBeat) {
            log.info("收到客户端心跳: {}", ctx.channel().remoteAddress());
            idleTime += 15;
            if(idleTime >= MAX_IDLE_TIME) {
                log.info("客户端长时间无业务请求，服务端主动关闭连接：{}", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else{
            throw new RuntimeException("不支持此类型的数据");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state().equals(IdleState.READER_IDLE)){
                log.info("客户端长时间未响应，关闭连接：{}", ctx.channel().remoteAddress());
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            System.out.println("客户端断开连接：" + ctx.channel().remoteAddress());
        } else {
            cause.printStackTrace(); // 其他异常打印
        }
    }

    private RpcResponse getResponse(RpcRequest request) {
        // 得到要请求的接口名
        String interfaceName = request.getInterfaceName();

        // 根据接口名进行限流
        RateLimit rateLimit = serviceProvider.getRateLimit(interfaceName);
        if(!rateLimit.getToken()){
            log.info("接口繁忙，请稍后再试");
            Counter limitCounter = meterRegistry.counter("rpc_limit_total");
            limitCounter.increment();
            return RpcResponse.fail(request.getRequestId());
        }

        // 得到服务端相应的接口实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用方法
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsType());
            Object invoke = method.invoke(service, request.getParams());
            return RpcResponse.success(invoke, request.getRequestId());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            log.error("方法执行错误");
            return RpcResponse.fail(request.getRequestId());
        }
    }

}
