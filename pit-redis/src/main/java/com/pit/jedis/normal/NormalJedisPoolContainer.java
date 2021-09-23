package com.pit.jedis.normal;

import com.pit.jedis.JedisPoolContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 普通的JedisPool容器，通过指定ip列表获取redis实例
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
public class NormalJedisPoolContainer implements JedisPoolContainer {
    /**
     * 需要reset计数器的值
     */
    private final static AtomicInteger counter = new AtomicInteger(0);
    private final static int INCR_UPPER_LIMIT = 1000000000;
    private String redisConnect;
    private String password;
    private GenericObjectPoolConfig jedisConfig;
    private int timeout;
    private AtomicBoolean isInit = new AtomicBoolean(false);

    private List<JedisPool> jedisPoolList = new ArrayList<>();

    public NormalJedisPoolContainer(String redisConnect, String password, int timeout, GenericObjectPoolConfig jedisPoolConfig) {
        this.redisConnect = redisConnect;
        this.password = password;
        this.jedisConfig = jedisPoolConfig;
        this.timeout = timeout;

        init();
    }

    public JedisPool getJedisPool() {
        if (jedisPoolList.size() == 1) {
            return jedisPoolList.get(0);
        }

        int index = counter.incrementAndGet();
        if (index > INCR_UPPER_LIMIT) {
            counter.set(0);
        }
        index = index % jedisPoolList.size();
        return jedisPoolList.get(index);
    }

    @Override
    public Jedis getClient() {
        if (!isInit.get()) {
            init();
        }
        return getJedisPool().getResource();
    }

    private void init() {
        if (isInit.compareAndSet(false, true)) {
            String[] redisServers = redisConnect.split(",");
            for (String ipAndPortStr : redisServers) {
                String[] ipAndPort = ipAndPortStr.split(":");
                String ip = ipAndPort[0];
                String port = ipAndPort[1];
                JedisPool jedisPool = null;
                if (StringUtils.isBlank(password)) {
                    jedisPool = new JedisPool(jedisConfig, ip, Integer.parseInt(port), timeout);
                } else {
                    jedisPool = new JedisPool(jedisConfig, ip, Integer.parseInt(port), timeout, password);
                }
                jedisPoolList.add(jedisPool);
            }
        }
    }

    @Override
    public void executeError(Jedis jedis) {

    }
}