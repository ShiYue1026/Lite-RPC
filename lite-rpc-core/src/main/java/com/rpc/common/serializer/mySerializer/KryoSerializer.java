package com.rpc.common.serializer.mySerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.sql.rowset.serial.SerialException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoSerializer implements Serializer {
    private static Kryo kryo;

    static {
        kryo = new Kryo();
    }

    @Override
    public byte[] serialize(Object obj) {
        if(obj == null) {
            throw new IllegalArgumentException("Cannot serialize null object");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Output output = new Output(baos);
            kryo.writeClassAndObject(output, obj);
            return output.toBytes();
        } catch (IOException e){
            throw new RuntimeException("Serialization failed");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) throws SerialException {
        if(bytes == null) {
            throw new IllegalArgumentException("Cannot deserialize null Object");
        }

        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            Input input = new Input(bais);
            return kryo.readClassAndObject(input);
        } catch (IOException e){
            throw new RuntimeException("Deserialization failed");
        }
    }

    @Override
    public int getType() {
        return 2;
    }
}
