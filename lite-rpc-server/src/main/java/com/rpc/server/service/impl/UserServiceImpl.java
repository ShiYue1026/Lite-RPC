package com.rpc.server.service.impl;

import com.rpc.common.annotation.RpcService;
import com.rpc.pojo.User;
import com.rpc.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.UUID;


@Slf4j
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUserByUserId(Integer id) {
        log.info("客户端查询了ID={}的用户", id);
        // 模拟从数据库中取用户的行为
        Random random = new Random();
        User user = User.builder()
                        .userName(UUID.randomUUID().toString())
                        .id(id)
                        .gender(random.nextBoolean()).build();
        log.info("返回用户信息: {}", user);
        return user;
    }

    @Override
    public Integer insertUserId(User user) {
        log.info("插入数据成功，用户名={}", user.getUserName());
        return user.getId();
    }
}
