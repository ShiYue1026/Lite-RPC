package com.rpc.common.serializer.mySerializer;

import javax.sql.rowset.serial.SerialException;

public interface Serializer {

    // 把对象序列化成字节数组
    byte[] serialize(Object obj);

    // 把字节数组反序列化成对象
    Object deserialize(byte[] bytes, int messageType) throws SerialException;

    // 0: java自带的序列化  1: json序列化
    int getType();

    static Serializer getSerializerByCode(int code) {
        switch(code) {
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            case 2:
                return new KryoSerializer();
            case 3:
                return new HessianSerializer();
            case 4:
                return new ProtostuffSerializer();
            default:
                return null;
        }
    }
}
