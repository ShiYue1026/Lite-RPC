# RPC原理

目标：客户端可以像调用本地方法一样调用远程服务端上的方法。

![image](https://github.com/user-attachments/assets/93a032c0-a8e7-46d0-af7a-30db7ecf5f97)



# RPC功能:
- 服务端和客户端完全解耦：服务端负责接口具体实现，客户端调用接口时，只需导入接口的依赖，而无需导入整个服务端

- 支持多种注册中心：[ZooKeeper](https://github.com/apache/zookeeper.git) 、[Nacos](https://github.com/alibaba/nacos.git)

- 支持客户端与服务端之间的高性能网络传输：[Netty](https://github.com/netty/netty.git)

- 支持客户端与服务端之间的多种序列化协议：[Json](https://www.json.org/json-en.html)、[Hessian](http://hessian.caucho.com/)、[Kryo](https://github.com/EsotericSoftware/kryo.git)、[Protostuff](https://github.com/protostuff/protostuff.git)

- 支持客户端与服务端之间的心跳超时与重连机制，自动管理客户端和服务端之间的连接状态

- 支持服务端上的多种限流算法：滑动窗口法、漏桶算法、令牌桶算法

- 支持客户端上的多种负载均衡策略：随机、轮询、一致性哈希

- 支持客户端自定义方法白名单的重试机制

- 支持客户端自定义熔断fallback处理

- 通过自定义`spring-boot-starter`组件的方式集成SpringBoot，支持通过注解的方式进行服务注册与服务消费

- 支持Prometheus + Grafana进行监控


  
# 使用方式

### 1. 引入依赖

```xml
<dependencies>
        // 封装好的Lite-RPC
        <dependency>
            <groupId>com.rpc</groupId>
            <artifactId>lite-rpc-core</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.rpc</groupId>
            <artifactId>lite-rpc-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

</dependencies>  

```

## 2. 服务端定义接口和实现类

### 2.1 定义接口

- 使用`@Retryable`注解标记需要进行重试的幂等方法

```java
public interface UserService {   // 客户端通过这个接口调用服务端的实现类

    @Retryable
    User getUserByUserId(Integer id);

    @Retryable
    Integer insertUserId(User user);

}
```

### 2.2 接口实现类

- 使用`@RpcService`注解标记接口实现类

```java
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
```

### 2.3 客户端可自定义FallBack类

- 在`@RpcClient`注解的参数中引入自定义的FallBack类

```java
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

```

```java
  ...
    @RpcClient(fallback = UserServiceFallBack.class)
    private UserService userService;
  ...
```

### 2.4 自定义配置

- 服务端自定义端口号和注册中心类型

```yaml
rpc:
  port: 6666
  registry: zookeeper        # 可选值: zookeeper(默认), nacos
```

- 客户端自定义序列化方式和负载均衡类型

```yaml
rpc:
  serializer: kryo           # 可选值: kryo(默认), hessian, protostuff
  loadBalance: roundrobin    # 可选值: roundrobin(默认), random, consistenthash
  registry: zookeeper        # 可选值: zookeeper(默认), nacos
```


### 2.5 客户端远程调用

- 使用`@RpcClient`注解标记需要远程调用的服务接口类
- 直接使用接口类调用服务类方法即可

```java
@Slf4j
@SpringBootTest
class ClientTest {
    
    @RpcClient(fallback = UserServiceFallBack.class)
    private UserService userService;

    @Test
    void clientTest() {
         User user = userService.getUserByUserId(i1);
         Integer id = userService.insertUserId(User.builder()
                            .id(i1)
                            .userName("User_" + i1)
                            .gender(true)
                            .build());
        }
    }
```

### 2.6 可以结合Prometheus + Grafana对远程调用状态进行监控
<img width="1061" alt="image" src="https://github.com/user-attachments/assets/06e107c9-a740-4aa6-90cb-6ea3ff09dab3" />



# 参考资料

https://github.com/youngyangyang04/RPC-Java.git

https://github.com/sofastack/sofa-rpc.git
