package Server;

import Server.provider.ServiceProvider;
import Server.server.RpcServer;
import Server.server.impl.NettyRpcServer;
import common.service.UserService;
import common.service.impl.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        String ip = System.getProperty("ip");
        int port = Integer.parseInt(System.getProperty("port"));

        UserService userService=new UserServiceImpl();

        ServiceProvider serviceProvider=new ServiceProvider(ip,port);
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer=new NettyRpcServer(serviceProvider);
        rpcServer.start(port);
    }
}
