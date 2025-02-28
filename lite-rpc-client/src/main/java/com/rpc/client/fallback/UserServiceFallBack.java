package com.rpc.client.fallback;

import com.rpc.pojo.User;
import com.rpc.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserServiceFallBack implements UserService {

    @Override
    public User getUserByUserId(Integer id) {
        log.info("getUserByUserId的fallback方法执行... userId={}", id);
        return null;
    }

    @Override
    public Integer insertUserId(User user) {
        log.info("insertUserId的fallback方法执行... userId={}", user.getId());
        return null;
    }
}

