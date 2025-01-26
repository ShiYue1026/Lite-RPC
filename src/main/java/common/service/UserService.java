package common.service;

import common.annotation.FallBack;
import common.annotation.Retryable;
import common.pojo.User;

@FallBack(handler = UserServiceFallBack.class)
public interface UserService {   // 客户端通过这个接口调用服务端的实现类

    @Retryable
    User getUserByUserId(Integer id);

    @Retryable
    Integer insertUserId(User user);

}
