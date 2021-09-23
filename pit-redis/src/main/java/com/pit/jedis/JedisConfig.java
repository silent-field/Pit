package com.pit.jedis;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPoolConfig;

/**
 * jedis 配置类
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
@Builder
@Data
public class JedisConfig {
    private int maxTotal = 64;
    private int maxWaitMills = 2000;
    private int maxIdle = 32;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;
    private boolean blockWhenExhausted = false;
    private boolean lifo = false;
    private int minEvictableIdleTime = 1800000;
    private int minIdle = 10;
    private int numTestsPerEvictionRun = 3;
    private int softMinEvictableIdleTimeMillis = 60000;
    private boolean testWhileIdle = true;
    private int timeBetweenEvictionRunsMillis = 6000;

    public GenericObjectPoolConfig getJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,
        jedisPoolConfig.setMaxWaitMillis(maxWaitMills);
        jedisPoolConfig.setMaxIdle(maxIdle);
        // 在获取连接的时候检查有效性, 默认false
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        // 在返回连接的时候检查有效性, 默认false
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        // 在空闲时检查有效性, 默认false
        jedisPoolConfig.setTestWhileIdle(testWhileIdle);
        // 连接耗尽时是否阻塞, false报异常,true阻塞直到超时, 默认true
        jedisPoolConfig.setBlockWhenExhausted(blockWhenExhausted);

        // 设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
//		jedisPoolConfig.setEvictionPolicyClassName(EvictionPolicyClassName);

        // 是否启用后进先出, 默认true
        jedisPoolConfig.setLifo(lifo);

        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        jedisPoolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTime);
        // 最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(minIdle);
        // 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        jedisPoolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        // 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数时直接逐出,不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        return jedisPoolConfig;
    }
}
