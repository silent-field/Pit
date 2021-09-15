package com.pit.core.limit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * 桶限流器实现
 *
 *
 * @Author: gy
 * @Date: 2021/9/14
 */
public class BucketLimiter {
    private final Cache<String, AtomicInteger> cache;

    /**
     * @param maxSize    桶大小
     * @param duration      记录缓存时间
     * @param timeUnit      单位时间
     * @return
     */
    public BucketLimiter(int maxSize, long duration, TimeUnit timeUnit) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(duration, timeUnit)
                .build();
    }

    public static BucketLimiter create(int bucketSize, long duration, TimeUnit timeUnit) {
        return new BucketLimiter(bucketSize, duration, timeUnit);
    }

    /**
     * 是否被限流 可根据不同的key 指定数量
     * @param key
     * @param limitSize
     * @return
     */
    public boolean tryAcquire(String key, int limitSize) {
        return acquire(key) > limitSize;
    }

    /**
     * 值自动原子性 +1 后判断是否被限流 可根据不同的key 指定数量
     * @param key
     * @param limitSize
     * @return
     */
    public boolean tryAcquireIncrement(String key, int limitSize) {
        return tryAcquire(key) > limitSize;
    }

    private int tryAcquire(String key) {
        try {
            AtomicInteger atomicInteger = cache.get(key, () -> new AtomicInteger(0));
            return atomicInteger.incrementAndGet();
        } catch (ExecutionException e) {
            throw new RuntimeException("BucketLimiter.tryAcquire exception", e);
        }
    }

    public int acquire(String key) {
        try {
            AtomicInteger atomicInteger = cache.get(key, () -> new AtomicInteger(0));
            return atomicInteger.get();
        } catch (ExecutionException e) {
            throw new RuntimeException("BucketLimiter.acquire exception", e);
        }
    }
}
