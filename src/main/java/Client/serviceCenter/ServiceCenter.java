package Client.serviceCenter;

import common.Message.RpcRequest;

import java.net.InetSocketAddress;

public interface ServiceCenter {

    // 服务发现：根据服务名查找地址
    InetSocketAddress serviceDiscovery(RpcRequest request);

}
