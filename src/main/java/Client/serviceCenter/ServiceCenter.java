package Client.serviceCenter;

import common.Message.RpcRequest;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public interface ServiceCenter {

    // 服务发现：根据服务名查找地址
    InetSocketAddress serviceDiscovery(RpcRequest request);

    // 判断是否可重试
    boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature);
}
