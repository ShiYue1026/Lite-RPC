package com.rpc.server.ratelimit;

import com.rpc.server.ratelimit.constant.RateLimitType;
import com.rpc.server.ratelimit.impl.TokenBucketRateLimiter;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class RateLimitFactory {

    private final HashMap<String, RateLimit> rateLimitMap = new HashMap<>();

    public RateLimit getRateLimit(String interfaceName, String type) {
        if(rateLimitMap.containsKey(interfaceName)) {
            return rateLimitMap.get(interfaceName);
        }
        type = type.toLowerCase();
        RateLimit rateLimit = createRateLimit(type);
        rateLimitMap.put(interfaceName, rateLimit);
        return rateLimit;
    }

    private RateLimit createRateLimit(String type) {
        switch(type){
            case RateLimitType.TOKEN_BUCKET:
                return new TokenBucketRateLimiter(10, 100);
            default:
                throw new IllegalArgumentException("Unknown rate limit type: " + type);
        }
    }
}
