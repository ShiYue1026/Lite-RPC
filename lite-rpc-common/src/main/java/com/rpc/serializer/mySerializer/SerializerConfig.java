package com.rpc.serializer.mySerializer;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SerializerConfig {

    @Getter
    @Value("${rpc.serializer}:Kryo")
    private static String serializerType;

}
