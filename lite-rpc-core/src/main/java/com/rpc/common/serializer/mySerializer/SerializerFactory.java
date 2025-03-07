package com.rpc.common.serializer.mySerializer;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

@Slf4j
public class SerializerFactory {

    private static final Map<String, Serializer> serializerMap = new HashMap<>();

    static {
        ServiceLoader.load(Serializer.class).forEach(serializer -> {
           serializerMap.put(serializer.getClass().getSimpleName().toLowerCase(), serializer);
        });
    }

    public static Serializer getSerializer(String name) {
        name = (name + "serializer" ).toLowerCase();
        return serializerMap.get(name);
    }

}
