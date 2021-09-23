package com.pit.jedis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import redis.clients.jedis.JedisPool;

/**
 * 一个Jedis连接池
 * @author gy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisEntry {
    private String address;

    private JedisPool jedisPool;

}
