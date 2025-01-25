package Server.serviceRegister;

import java.net.InetSocketAddress;

public interface ServiceRegister {

    // 服务注册
    void register(Class<?> clazz, InetSocketAddress serviceAddress);

}
