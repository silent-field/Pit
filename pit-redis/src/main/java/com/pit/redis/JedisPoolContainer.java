package com.pit.redis;

import redis.clients.jedis.Jedis;

/**
 * Jedis连接池容器
 * @author gy
 * @version 1.0
 * @date 2020/7/7.
 */
public interface JedisPoolContainer {
    /**
     * 获取一个Jedis实例
     *
     * @return
     */
    Jedis getClient();

    /**
     * 调用失败触发
     *
     * @param jedis Jedis实例
     */
    void executeError(Jedis jedis);
}
