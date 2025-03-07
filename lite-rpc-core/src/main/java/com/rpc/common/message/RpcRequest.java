package com.rpc.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcRequest implements Serializable {

    // 请求id
    private String requestId;

    // 服务类名，客户端只知道接口
    private String interfaceName;

    // 调用的方法名
    private String methodName;

    // 参数列表
    private Object[] params;

    // 参数类型
    private Class<?>[] paramsType;
}
