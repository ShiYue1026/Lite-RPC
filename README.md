# RPC原理

![image](https://github.com/user-attachments/assets/93a032c0-a8e7-46d0-af7a-30db7ecf5f97)



# RPC功能:

- 支持多种注册中心：[ZooKeeper](https://github.com/apache/zookeeper.git) 、[Nacos](https://github.com/alibaba/nacos.git)

- 支持高性能网络传输：[Netty](https://github.com/netty/netty.git)

- 支持多种序列化协议：[Json](https://www.json.org/json-en.html)、[Hessian](http://hessian.caucho.com/)、[Kryo](https://github.com/EsotericSoftware/kryo.git)、[Protostuff](https://github.com/protostuff/protostuff.git)

- 支持多种负载均衡策略：（加权）随机、（加权）轮询、一致性哈希

- 支持多种限流算法：滑动窗口法、漏桶算法、令牌桶算法

- 支持基于方法白名单的重试机制

- 支持自定义熔断fallback处理

- 支持心跳超时机制，自动管理客户端和服务端之间的连接状态

- 支持自定义客户端与服务端的延迟连接与非延迟连接

- 支持通过Spring注解的方式进行服务注册与服务消费
  
# 使用方式
### 1. 下载模块

### 2. 引入依赖

```xml
<dependencies>
    	<dependency>
            <groupId>com.rpc</groupId>
            <artifactId>lite-rpc-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
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

## 3. 服务端定义接口和实现类

### 3.1 导入模块

### 3.2 定义接口

- 使用`@Retryable`注解标记需要进行重试的幂等方法

```java
@FallBack(handler = UserServiceFallBack.class)  // 
public interface UserService {   // 客户端通过这个接口调用服务端的实现类

    @Retryable
    User getUserByUserId(Integer id);

    @Retryable
    Integer insertUserId(User user);

}
```

- 使用`@FallBack`注解标记自定义的FallBack类

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

### 3.3 接口实现类

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

### 3.4 服务端和客户端引入依赖

```xml
<dependencies>
        <dependency>
            <groupId>com.rpc</groupId>
            <artifactId>lite-rpc-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
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

### 3.5 自定义配置

- 服务端自定义端口号和注册中心类型

```yaml
rpc:
  port: 6666
  registry: zookeeper
```

- 客户端自定义序列化方式和负载均衡类型

```yaml
rpc:
  serializer: hessian
  loadBalance: random
```


### 3.6 客户端远程调用

- 使用`@RpcClient`注解标记需要远程调用的服务接口类
- 直接使用接口类调用服务类方法即可

```java
@Slf4j
@SpringBootTest
class ClientTest {
    
    @RpcClient
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




# 参考资料

https://github.com/youngyangyang04/RPC-Java.git

https://github.com/sofastack/sofa-rpc.git
