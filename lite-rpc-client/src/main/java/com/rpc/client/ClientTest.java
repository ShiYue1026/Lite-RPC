package com.rpc.client;

import com.rpc.common.annotation.RpcClient;
import com.rpc.client.fallback.UserServiceFallBack;
import com.rpc.pojo.User;
import com.rpc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ClientTest implements CommandLineRunner {

    private static final int THREAD_POOL_SIZE = 20;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @RpcClient(fallback = UserServiceFallBack.class)
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        log.info("RPC 客户端启动，开始并发请求...");

        for (int i = 0; i < 100; i++) {
            if(i % 50 == 0 && i != 0){
                Thread.sleep(3000);
            }
            Integer i1 = i;
            executorService.execute(() -> {
                try {
                    // 发送 RPC 请求
                    User user = userService.getUserByUserId(i1);
                    if (user != null) {
                        log.info("从服务端得到的 user={}", user);
                    } else {
                        log.warn("获取的 user 为 null, userId={}", i1);
                    }

                    // 发送插入请求
                    Integer id = userService.insertUserId(User.builder()
                            .id(i1)
                            .userName("User_" + i1)
                            .gender(true)
                            .build());

                    if (id != null) {
                        log.info("向服务端插入 user 的 id={}", id);
                    } else {
                        log.warn("插入失败，返回的 id 为 null, userId={}", i1);
                    }
                } catch (Exception e) {
                    log.error("调用 RPC 失败, userId={}", i1, e);
                }
            });
        }

        Thread.sleep(30000);

        for (int i = 0; i < 100; i++) {
            if(i % 50 == 0 && i != 0){
                Thread.sleep(10000);
            }
            Integer i1 = i;
            executorService.submit(() -> {
                try {
                    // 发送 RPC 请求
                    User user = userService.getUserByUserId(i1);
                    if (user != null) {
                        log.info("从服务端得到的 user={}", user);
                    } else {
                        log.warn("获取的 user 为 null, userId={}", i1);
                    }

                    // 发送插入请求
                    Integer id = userService.insertUserId(User.builder()
                            .id(i1)
                            .userName("User_" + i1)
                            .gender(true)
                            .build());

                    if (id != null) {
                        log.info("向服务端插入 user 的 id={}", id);
                    } else {
                        log.warn("插入失败，返回的 id 为 null, userId={}", i1);
                    }
                } catch (Exception e) {
                    log.error("调用 RPC 失败, userId={}", i1, e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        log.info("所有任务执行完毕");
    }
}
