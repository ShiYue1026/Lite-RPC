package Server.ratelimit.impl;

import Server.ratelimit.RateLimit;

public class TokenBucketRateLimiter implements RateLimit {

    // 令牌产生速率（单位ms）
    private static int RATE;

    // 令牌桶最大容量
    private static int CAPACITY;

    // 当前令牌桶容量
    private volatile int curCapacity;

    // 上次请求的时间戳
    private volatile long timeStamp = System.currentTimeMillis();

    public TokenBucketRateLimiter(int rate, int capacity) {
        RATE = rate;
        CAPACITY = capacity;
        curCapacity = capacity;
    }

    @Override
    public synchronized boolean getToken() {
        if(curCapacity > 0){
            curCapacity--;
            return true;
        }

        // 当前没有空余的令牌
        long currentTime = System.currentTimeMillis();
        if(currentTime - timeStamp > RATE){  // 可以产生新的令牌了
            if((currentTime - timeStamp) / RATE >= 2){
                curCapacity += (int) (curCapacity - timeStamp) / RATE - 1;  // -1是因为当前这次请求也会消耗一个令牌
            }

            if(curCapacity > CAPACITY) {
                curCapacity = CAPACITY;
            }
            timeStamp = currentTime;
            return true;
        }

        return false;
    }

}
