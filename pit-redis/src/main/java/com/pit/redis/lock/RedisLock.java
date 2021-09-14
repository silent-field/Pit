package com.pit.redis.lock;

import com.pit.core.net.LocalIpHolder;
import com.pit.core.time.CachingSystemTimer2;
import com.pit.redis.RedisClient;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ThreadLocalRandom;

/**
 * redis分布式锁
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
public class RedisLock {
    private RedisClient redisClient;

    public static long justWait(Long sleepTime) {
        try {
            Thread.sleep(sleepTime, ThreadLocalRandom.current().nextInt(Math.max((int) (sleepTime >> 5), 1))); // 除以32
        } catch (InterruptedException e) {
            // nothing to do
        }
        return sleepTime << 1;
    }

    public void RedisLock(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    /**
     * 尝试获取锁（包含解锁操作）
     *
     * @param <T>
     * @param lockCallback callback
     * @param key          锁的key
     * @param timeout      超时时间px
     * @return
     */
    public <T> Pair<Boolean, T> tryLock(LockCallback<T> lockCallback, String key, Long timeout) {
        if (redisClient.setNX(key, LocalIpHolder.getInstanceId(), timeout)) {
            long begin = CachingSystemTimer2.getNow();
            try {
                return ImmutablePair.of(true, lockCallback.exec());
            } finally {
                unlock(key, begin, timeout);
            }
        } else {
            return ImmutablePair.of(false, null);
        }
    }

    /**
     * 尝试获取锁（不包含解锁操作，需手动解锁）
     *
     * @param key     锁的key
     * @param timeout 超时时间px
     * @return
     */
    public Pair<Boolean, Long> tryLock(String key, Long timeout) {
        if (redisClient.setNX(key, LocalIpHolder.getInstanceId(), timeout)) {
            return ImmutablePair.of(true, CachingSystemTimer2.getNow());
        }
        return ImmutablePair.of(false, CachingSystemTimer2.getNow());
    }

    /**
     * 尝试获取锁（包含解锁操作）
     *
     * @param <T>
     * @param lockCallback callback
     * @param key          锁的key
     * @param timeout      超时时间px
     * @return
     */
    public <T> Pair<Boolean, T> lock(LockCallback<T> lockCallback, String key, Long timeout) {
        long waitTime = Math.max(timeout >> 3, 1);// 除以8
        long milliseconds = CachingSystemTimer2.getNow();
        do {
            if (redisClient.setNX(key, LocalIpHolder.getInstanceId(), timeout)) {
                long begin = CachingSystemTimer2.getNow();
                try {
                    return ImmutablePair.of(true, lockCallback.exec());
                } finally {
                    unlock(key, begin, timeout);
                }
            }
            waitTime = justWait(waitTime);
        } while (CachingSystemTimer2.getNow() - milliseconds < timeout);
        return ImmutablePair.of(false, null);
    }

    /**
     * 尝试获取锁（不包含解锁操作，需手动解锁）
     *
     * @param key     锁的key
     * @param timeout 超时时间px
     * @return
     */
    public Pair<Boolean, Long> lock(String key, long timeout) {
        long waitTime = Math.max(timeout >> 3, 1);// 除以8
        long milliseconds = CachingSystemTimer2.getNow();
        do {
            if (redisClient.setNX(key, LocalIpHolder.getInstanceId(), timeout)) {
                return ImmutablePair.of(true, milliseconds);
            }
            waitTime = justWait(waitTime);
        } while (CachingSystemTimer2.getNow() - milliseconds < timeout);
        return ImmutablePair.of(false, milliseconds);
    }

    /**
     * 解锁
     *
     * @param key
     */
    public void unlock(String key) {
        redisClient.del(key);
    }

    /**
     * 安全解锁
     *
     * @param key
     * @param begin   锁开始的时间
     * @param timeout 超时时间px
     */
    public void unlock(String key, long begin, long timeout) {
        // 是自己才解锁
//		if (LocalIpHolder.getInstanceId().equals(redisClient.get(key))) {
//			return;
//		}
        // 未超时才解锁
        if (CachingSystemTimer2.getNow() - begin < timeout) {
            redisClient.del(key);
        }
    }
}
