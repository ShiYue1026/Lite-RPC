package com.rpc.serializer.mySerializer;

import com.rpc.exception.SerializeException;
import com.rpc.message.RpcHeartBeat;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import io.protostuff.LinkedBuffer;
import io.protostuff.Schema;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

import javax.sql.rowset.serial.SerialException;
import java.util.Objects;

import static com.rpc.message.MessageType.*;

public class ProtostuffSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        if(obj == null){
            throw new IllegalArgumentException("Cannot serialize null object");
        }

        Schema schema = RuntimeSchema.getSchema(obj.getClass());

        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] bytes;
        try{
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) throws SerialException {
        if(bytes == null) {
            throw new IllegalArgumentException("Cannot deserialize null Object");
        }
        Class<?> clazz = null;
        if(Objects.equals(messageType, REQUEST.getCode())){
            clazz = RpcRequest.class;
        }
        else if(Objects.equals(messageType, RESPONSE.getCode())){
            clazz = RpcResponse.class;
        }
        else if(Objects.equals(messageType, HEARTBEAT.getCode())){
            clazz = RpcHeartBeat.class;
        }
        Schema schema = RuntimeSchema.getSchema(clazz);

        Object obj;
        try {
            obj =clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new SerializeException("Deserialization failed due to reflection issues");
        }

        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }

    @Override
    public int getType() {
        return 4;
    }
}
