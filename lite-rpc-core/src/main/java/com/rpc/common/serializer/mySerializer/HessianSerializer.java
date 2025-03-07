package com.rpc.common.serializer.mySerializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.rpc.common.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(baos);
            hessianOutput.writeObject(obj);
            hessianOutput.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Hessian serialization failed", e);
            throw new SerializeException("Serialization failed", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(bais);
            Object obj = hessianInput.readObject();
            log.info("Deserialized object of type: {}", obj.getClass().getName());
            return obj;
        } catch (IOException e) {
            log.error("Hessian deserialization failed", e);
            throw new SerializeException("Deserialization failed", e);
        }
    }

    @Override
    public int getType() {
        return 3;
    }
}
