package com.rpc.client.circuitbreaker;

public enum CircuitBreakerStatus {
    CLOSED,
    OPEN,
    HALF_OPEN;
}
