package com.rpc.message;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageType {
    REQUEST(0), RESPONSE(1), HEARTBEAT(2);

    private final int code;

    public int getCode(){
        return code;
    }
}
