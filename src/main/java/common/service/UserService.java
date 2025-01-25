package common.service;

import Client.retry.annotation.Retryable;
import common.pojo.User;

public interface UserService {   // 客户端通过这个接口调用服务端的实现类

    @Retryable
    User getUserByUserId(Integer id);

    Integer insertUserId(User user);

}
