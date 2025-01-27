package com.rpc.client;

import com.rpc.client.proxy.ClientProxy;
import com.rpc.pojo.User;
import com.rpc.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ClientTest {

    private static final int THREAD_POOL_SIZE = 20;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws InterruptedException {
        ClientProxy clientProxy=new ClientProxy();
        //ClientProxy clientProxy=new part2.Client.proxy.ClientProxy("127.0.0.1",9999,0);  // 不需要再写入指定的ip和port了
        UserService proxy=clientProxy.getProxy(UserService.class);

        for(int i=0; i<150; i++){
            Integer i1 = i;
            if(i % 50 == 0){
                Thread.sleep(10000);
            }
            executorService.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(i1);
                    if(user != null){
                        log.info("从服务端得到的user={}", user);
                    } else{
                        log.warn("获取的 user 为 null, userId={}", i1);
                    }

                    Integer id = proxy.insertUserId(User.builder()
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

        // clientProxy.rpcClient.stop();
    }
}
