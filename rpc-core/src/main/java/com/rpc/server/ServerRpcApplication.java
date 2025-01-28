package com.rpc.server;

import com.rpc.config.RpcConfig;
import com.rpc.config.RpcConstant;
import com.rpc.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerRpcApplication {

    private static volatile RpcConfig rpcConfig;

    public static void initialize(RpcConfig customRpcConfig) {
        rpcConfig = customRpcConfig;
        log.info("RPC服务端框架初始化，配置 = {}", customRpcConfig);
    }

    public static void initialize() {
        if(rpcConfig != null){
            initialize(rpcConfig);
        }
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
            synchronized (ServerRpcApplication.class){
                if(rpcConfig == null){
                    initialize();
                }
            }
        }
        return rpcConfig;
    }
}
