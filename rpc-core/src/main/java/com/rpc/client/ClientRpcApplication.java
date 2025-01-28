package com.rpc.client;

import com.rpc.config.RpcConfig;
import com.rpc.config.RpcConstant;
import com.rpc.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientRpcApplication {

    private static volatile RpcConfig rpcConfig;

    public static void initialize(RpcConfig customRpcConfig) {
        rpcConfig = customRpcConfig;
        log.info("RPC服务端框架初始化，配置 = {}", customRpcConfig);
    }

    public static void initialize() {
        RpcConfig customRpcConfig;
        try{
            customRpcConfig = ConfigUtil.loadConfig(RpcConfig.class, RpcConstant.CONFIG_PREFIX);
        } catch (Exception e){
            customRpcConfig = new RpcConfig();
            log.warn("配置加载失败，使用默认配置");
        }
        initialize(customRpcConfig);
    }

    public static RpcConfig getRpcConfig() {
        if(rpcConfig == null){
            synchronized (ClientRpcApplication.class){
                if(rpcConfig == null){
                    initialize();
                }
            }
        }
        return rpcConfig;
    }
}
