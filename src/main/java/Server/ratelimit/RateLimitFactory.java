package Server.ratelimit;


import Server.ratelimit.constant.RateLimitType;
import Server.ratelimit.impl.TokenBucketRateLimiter;

import java.util.HashMap;

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
                return new TokenBucketRateLimiter(100, 200);
            default:
                throw new IllegalArgumentException("Unknown rate limit type: " + type);
        }
    }
}
