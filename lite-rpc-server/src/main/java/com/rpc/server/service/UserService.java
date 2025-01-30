package com.rpc.server.service;


import com.rpc.annotation.FallBack;
import com.rpc.annotation.Retryable;
import com.rpc.server.pojo.User;

@FallBack(handler = UserServiceFallBack.class)
public interface UserService {   // 客户端通过这个接口调用服务端的实现类

    @Retryable
    User getUserByUserId(Integer id);

    @Retryable
    Integer insertUserId(User user);

}
