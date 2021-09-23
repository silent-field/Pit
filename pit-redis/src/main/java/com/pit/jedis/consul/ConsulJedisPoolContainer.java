package com.pit.redis.consul;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pit.consul.ConsulClient;
import com.pit.redis.JedisPoolContainer;
import com.pit.redis.RedisEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通过Consul注册发现的JedisPool容器
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
@Slf4j
public class ConsulJedisPoolContainer implements JedisPoolContainer {
    /**
     * 需要reset计数器的值
     */
    private final static AtomicInteger addCounter = new AtomicInteger(0);
    private final static int INCR_UPPER_LIMIT = 1000000000;

    private static volatile boolean isInit = false;

    private static List<RedisEntry> jedisPoolList = new LinkedList<>();
    /**
     * Redis ip 实例错误计数器，10分钟刷新
     */
    private static LoadingCache<String, AtomicInteger> errorAddrCounter = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).concurrencyLevel(32).initialCapacity(8)
            .build(new CacheLoader<String, AtomicInteger>() {
                @Override
                public AtomicInteger load(String key) {
                    return new AtomicInteger(0);
                }
            });
    /**
     * 1小时内被移除的Redis ip 实例
     */
    private static Cache<String, Boolean> removeAddr = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(100).concurrencyLevel(32).initialCapacity(8).build();
    private String consulHosts;
    private String consulRedisName;
    private String password;
    private int timeout;
    private GenericObjectPoolConfig jedisPoolConfig;
    private int errorMaxTimes = 3;
    private int minIdle = 5;
    private int consulRedisMaxSize = 10;

    public ConsulJedisPoolContainer(String consulHosts, String consulRedisName, String password, int timeout, GenericObjectPoolConfig jedisPoolConfig) {
        this.consulHosts = consulHosts;
        this.consulRedisName = consulRedisName;
        this.password = password;
        this.timeout = timeout;
        this.jedisPoolConfig = jedisPoolConfig;
        init();
    }

    public JedisPool getJedisPool() {
        for (int i = 0; i < 3; i++) {
            int index = addCounter.incrementAndGet();
            if (index > INCR_UPPER_LIMIT) {
                addCounter.set(0);
            }
            index = index % jedisPoolList.size();
            RedisEntry entry = jedisPoolList.get(index);
            if (null == entry) {
                continue;
            }
            try {
                return entry.getJedisPool();
            } catch (Exception e) {
                log.error("redis get redis client fail", e);
                continue;
            }
        }
        return jedisPoolList.get(0).getJedisPool();
    }

    @Override
    public Jedis getClient() {
        if (!isInit) {
            init();
        }
        return getJedisPool().getResource();
    }

    @Override
    public void executeError(Jedis jedis) {
        if (null == jedis) {
            return;
        }
        String host = jedis.getClient().getHost();
        int port = jedis.getClient().getPort();
        String key = host + ":" + port;
        final int max = errorMaxTimes;
        try {
            int error = errorAddrCounter.get(key).incrementAndGet();
            if (error >= max) {
                broken(key);
            }
        } catch (Exception e) {
            log.error("redis errAddr get fail");
        }
    }

    /**
     * redis实例挂了换新
     */
    public synchronized void broken(String key) {
        // 如果本来只剩下5个，则不会去掉
        if (jedisPoolList.size() <= minIdle) {
            return;
        }

        // 查找要remove的entry
        RedisEntry removeEntry = null;
        for (RedisEntry entry : jedisPoolList) {
            if (key.equals(entry.getAddr())) {
                removeEntry = entry;
                break;
            }
        }

        // 如果没有要remove节点就退出
        if (null == removeEntry) {
            return;
        }

        // 执行remove
        log.warn("consul redis removing, key {}", key);
        jedisPoolList.remove(removeEntry);
        removeAddr.put(key, true);
        errorAddrCounter.refresh(key);
        try {
            removeEntry.getJedisPool().close();
        } catch (Exception e) {
            log.error("closing jedis pool error:", key, e);
        }
        // 查找所有redis的节点
        ConsulClient c = ConsulClient.instance(consulHosts);
        Set<String> servers = c.serviceGet(consulRedisName);
        OUTER:
        for (String server : servers) {
            // 如果removeAddr上有则不要
            if (null != removeAddr.getIfPresent(server) && removeAddr.getIfPresent(server)) {
                continue;
            }
            // 如果现在有也不要
            for (RedisEntry entry : jedisPoolList) {
                if (server.equals(entry.getAddr())) {
                    continue OUTER;
                }
            }
            // 初始化redis
            try {
                initRedis(server);
                break;
            } catch (Exception e) {
                log.error("init redis error:", server, e);
            }
        }
    }

    private synchronized void init() {
        if (isInit) {
            return;
        }
        for (String ipAndPortStr : getRedisServers()) {
            try {
                initRedis(ipAndPortStr);
            } catch (Exception e) {
                log.error("init redis error:", ipAndPortStr, e);
            }
        }
        isInit = true;
    }

    /**
     * 初始化redis并放入list
     *
     * @param ipAndPortStr
     */
    private void initRedis(String ipAndPortStr) {
        log.info("consul redis init start {}", ipAndPortStr);
        String[] ipAndPort = ipAndPortStr.split(":");
        String ip = ipAndPort[0];
        String port = ipAndPort[1];
        JedisPool jedisPool;
        if (StringUtils.isBlank(password)) {
            jedisPool = new JedisPool(jedisPoolConfig, ip, Integer.parseInt(port), timeout);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, ip, Integer.parseInt(port), timeout, password);
        }
        RedisEntry redisEntry = new RedisEntry(ipAndPortStr, jedisPool);
        jedisPoolList.add(redisEntry);
    }

    /**
     * consul获取 redis ip 列表
     *
     * @return
     */
    private String[] getRedisServers() {

        ConsulClient c = ConsulClient.instance(consulHosts);
        Set<String> servers = c.serviceGet(consulRedisName);
        int max = consulRedisMaxSize;
        List<String> serverList = new ArrayList<>(max);
        if (servers.size() > 3 * max) {
            // 如果远大于max，则随机取一个值开始取
            int i = 0;
            int skip = ThreadLocalRandom.current().nextInt(servers.size() - max - 1);
            for (String server : servers) {
                i++;
                if (i <= skip) {
                    continue;
                }
                serverList.add(server);
            }
        } else if (servers.size() > max) {
            // 如果稍微大于max，则取最后的几个
            int i = 0;
            int skip = servers.size() - max;
            for (String server : servers) {
                i++;
                if (i <= skip) {
                    continue;
                }
                serverList.add(server);
            }
        } else {
            // 如果数量小于等于max则直接加入
            serverList.addAll(servers);
        }
        return serverList.toArray(new String[0]);
    }
}
