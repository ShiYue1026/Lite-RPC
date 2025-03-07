package com.rpc.service;

import com.rpc.common.annotation.Retryable;
import com.rpc.pojo.User;

public interface UserService {   // 客户端通过这个接口调用服务端的实现类

    @Retryable
    User getUserByUserId(Integer id);

    @Retryable
    Integer insertUserId(User user);

}
