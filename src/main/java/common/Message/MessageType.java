package common.Message;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageType {
    REQUEST(0),RESPONSE(1);

    private final int code;

    public int getCode(){
        return code;
    }
}
