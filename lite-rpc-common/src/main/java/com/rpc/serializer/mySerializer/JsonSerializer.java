package com.rpc.serializer.mySerializer;

import com.alibaba.fastjson.JSONObject;
import com.rpc.message.RpcHeartBeat;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static com.rpc.message.MessageType.*;

@Slf4j
public class JsonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        return JSONObject.toJSONBytes(obj);
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        if (Objects.equals(messageType, REQUEST.getCode())) {
            RpcRequest request = JSONObject.parseObject(bytes, RpcRequest.class);
            Object[] objects = new Object[request.getParams().length];
            // 对转换后的request中的params属性逐个进行类型判断
            for(int i=0; i<objects.length; i++) {
                Class<?> paramsType = request.getParamsType()[i];
                // 判断每个对象类型是否和paramsTypes中的一致
                if(!paramsType.isAssignableFrom(request.getParams()[i].getClass())) {
                    objects[i] = JSONObject.toJavaObject((JSONObject)request.getParams()[i], paramsType);
                } else{
                    objects[i] = request.getParams()[i];
                }
            }
            request.setParams(objects);
            obj = request;
        } else if(Objects.equals(messageType, RESPONSE.getCode())) {
            RpcResponse response = JSONObject.parseObject(bytes, RpcResponse.class);
            Class<?> dataType = response.getDataType();
            // 对转换后的data的数据类型进行判断
            if (response.getData() != null && !dataType.isAssignableFrom(response.getData().getClass())) {
                response.setData(JSONObject.toJavaObject((JSONObject) response.getData(), dataType));
            }
            obj = response;
        } else if(Objects.equals(messageType, HEARTBEAT.getCode())){
            obj = JSONObject.<RpcHeartBeat>parseObject(bytes, RpcHeartBeat.class);
        }
        else {
            log.info("JSON反序列化器暂时不支持此种类型");
            throw new RuntimeException();
        }
        return obj;
    }

    @Override
    public int getType() {
        return 1;
    }
}
