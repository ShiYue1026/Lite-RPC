package com.rpc.config;


import com.rpc.serializer.mySerializer.Serializer;
import com.rpc.server.serviceRegister.impl.ZKServiceRegister;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcConfig {
    //服务名称
    private String name = "";
    //端口
    private Integer port = 9999;
    //主机名
    private String host = "localhost";
    //注册中心
    private String registry = "zookeeper";
    //序列化器
    private String serializer = "json";
    //负载均衡
    private String loadBalance = "roundrobin";
}
