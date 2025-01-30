package com.rpc.client;

import com.rpc.annotation.RpcClient;


import com.rpc.server.pojo.User;
import com.rpc.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
@SpringBootTest
class ClientTest {

    private static final int THREAD_POOL_SIZE = 20;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @RpcClient
    private UserService userService;

    @Test
    void clientTest() throws InterruptedException {

        for(int i=0; i<150; i++){
            Integer i1 = i;
            if(i % 50 == 0){
                Thread.sleep(10000);
            }
            executorService.submit(() -> {
                try {
                    User user = userService.getUserByUserId(i1);
                    if(user != null){
                        log.info("从服务端得到的user={}", user);
                    } else{
                        log.warn("获取的 user 为 null, userId={}", i1);
                    }

                    Integer id = userService.insertUserId(User.builder()
                            .id(i1)
                            .userName("User_" + i1)
                            .gender(true)
                            .build());

                    if(id != null){
                        log.info("向服务端插入user的id={}", id);
                    } else{
                        log.warn("插入失败，返回的id为null, userId={}", i1);
                    }
                } catch (Exception e){
                    log.error("调用服务时发生异常, userId={}",i1, e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);  // 等待所有任务执行完毕，避免测试关闭
        // clientProxy.rpcClient.stop();
    }

}