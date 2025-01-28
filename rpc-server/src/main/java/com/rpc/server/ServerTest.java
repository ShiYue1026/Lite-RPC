package com.rpc.server;

import com.rpc.server.impl.UserServiceImpl;
import com.rpc.server.provider.ServiceProvider;
import com.rpc.server.rpcserver.RpcServer;
import com.rpc.server.rpcserver.impl.NettyRpcServer;
import com.rpc.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerTest {
    public static void main(String[] args) {
        ServerRpcApplication.initialize();

        String ip = ServerRpcApplication.getRpcConfig().getHost();
        int port = Integer.parseInt(System.getProperty("port"));

        UserService userService=new UserServiceImpl();

        ServiceProvider serviceProvider=new ServiceProvider(ip, port);
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer=new NettyRpcServer(serviceProvider);
        rpcServer.start(port);
    }
}
