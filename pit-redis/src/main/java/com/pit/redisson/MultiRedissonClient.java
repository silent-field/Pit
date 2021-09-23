package com.pit.redisson;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: 封装多个redis client，用于灾备
 * @Author: gy
 * @Date: 2021/9/23
 */
@Slf4j
public class MultiRedissonClient implements RedissonClient {
    /**
     * 需要reset计数器的值
     */
    private final static AtomicInteger counter = new AtomicInteger(0);
    private final static int INCR_UPPER_LIMIT = 1000000000;

    private String redissonConnect;
    private String password;

    private ImmutableList<RedissonClient> redissonClients = ImmutableList.of();
    private volatile ImmutableList<RedissonClient> healthyRedissonClients = ImmutableList.of();
    /** 默认的redisson健康检查时间间隔：3s */
    private static final long DEFAULT_HEALTH_CHECK_PERIOD_MS = 3000;

    public MultiRedissonClient(String redissonConnect, String password) {
        this.redissonConnect = redissonConnect;
        this.password = password;

        init();
    }

    private void init() {
        String[] redissonServers = redissonConnect.split(",");
        ImmutableList.Builder<RedissonClient> builder = ImmutableList.builder();
        for (String instance : redissonServers) {
            Config config = new Config();
            MasterSlaveServersConfig masterSlaveServersConfig = config.useMasterSlaveServers().setMasterAddress(instance);

            if (StringUtils.isNotBlank(password)) {
                masterSlaveServersConfig.setPassword(password);
            }

            builder.add(Redisson.create(config));
        }
        redissonClients = builder.build();

        healthCheck();
        startHealthCheck();
    }

    /**
     * 开启连接池健康检查
     */
    private void startHealthCheck() {
        ScheduledThreadPoolExecutor poolHealthCheckExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().
                setNameFormat("Redisson Health Check Thread").setDaemon(true).build());
        poolHealthCheckExecutor.scheduleAtFixedRate(this::healthCheck,
                DEFAULT_HEALTH_CHECK_PERIOD_MS, DEFAULT_HEALTH_CHECK_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 健康检查
     */
    private void healthCheck() {
        log.debug("health check at: {}", System.currentTimeMillis());
        ImmutableList.Builder<RedissonClient> builder = ImmutableList.builder();
        for (RedissonClient redissonClient : redissonClients) {
            if (isHealthy(redissonClient)) {
                builder.add(redissonClient);
            } else {
                log.error("redisson attr:" + redissonClient + " is unhealthy");
            }
        }
        ImmutableList<RedissonClient> newHealthPools = builder.build();
        log.debug("finish health check at: {}", System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(newHealthPools)) {
            healthyRedissonClients = newHealthPools;
        } else {
            log.error("All redisson are unhealthy");
        }
    }

    private RedissonClient getClient() {
        ImmutableList<RedissonClient> clients = this.healthyRedissonClients;

        // 如果没有健康的实例，使用redissonClients
        if (clients.isEmpty()) {
            log.error("redisson healthy pool is empty,use common pools");
            clients = redissonClients;
        }

        if (clients.size() == 1) {
            return clients.get(0);
        }

        int index = counter.incrementAndGet();
        if (index > INCR_UPPER_LIMIT) {
            counter.set(0);
        }
        index = index % clients.size();

        return clients.get(index);
    }

    /**
     * 执行健康检查
     *
     * @return true:连接池正常
     */
    public boolean isHealthy(RedissonClient redissonClient) {
        int retryTime = 0;
        while (retryTime++ < 3) {
            try {
                redissonClient.getBucket("test").isExists();
                return true;
            } catch (Exception e) {
                log.error("redisson attr:" + redissonClient + " health check failed(" + retryTime + ")! reason: " + e.getMessage(), e);
            }
        }
        log.error("redisson attr: " + redissonClient + " health check failed!");
        return false;
    }

    @Override
    public <V> RTimeSeries<V> getTimeSeries(String name) {
        return getClient().getTimeSeries(name);
    }

    @Override
    public <V> RTimeSeries<V> getTimeSeries(String name, Codec codec) {
        return getClient().getTimeSeries(name, codec);
    }

    @Override
    public <K, V> RStream<K, V> getStream(String name) {
        return getClient().getStream(name);
    }

    @Override
    public <K, V> RStream<K, V> getStream(String name, Codec codec) {
        return getClient().getStream(name, codec);
    }

    @Override
    public RRateLimiter getRateLimiter(String name) {
        return getClient().getRateLimiter(name);
    }

    @Override
    public RBinaryStream getBinaryStream(String name) {
        return getClient().getBinaryStream(name);
    }

    @Override
    public <V> RGeo<V> getGeo(String name) {
        return getClient().getGeo(name);
    }

    @Override
    public <V> RGeo<V> getGeo(String name, Codec codec) {
        return getClient().getGeo(name, codec);
    }

    @Override
    public <V> RSetCache<V> getSetCache(String name) {
        return getClient().getSetCache(name);
    }

    @Override
    public <V> RSetCache<V> getSetCache(String name, Codec codec) {
        return getClient().getSetCache(name, codec);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec) {
        return getClient().getMapCache(name, codec);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec, MapOptions<K, V> options) {
        return getClient().getMapCache(name, codec, options);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name) {
        return getClient().getMapCache(name);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, MapOptions<K, V> options) {
        return getClient().getMapCache(name, options);
    }

    @Override
    public <V> RBucket<V> getBucket(String name) {
        return getClient().getBucket(name);
    }

    @Override
    public <V> RBucket<V> getBucket(String name, Codec codec) {
        return getClient().getBucket(name, codec);
    }

    @Override
    public RBuckets getBuckets() {
        return getClient().getBuckets();
    }

    @Override
    public RBuckets getBuckets(Codec codec) {
        return getClient().getBuckets(codec);
    }

    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name) {
        return getClient().getHyperLogLog(name);
    }

    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name, Codec codec) {
        return getClient().getHyperLogLog(name, codec);
    }

    @Override
    public <V> RList<V> getList(String name) {
        return getClient().getList(name);
    }

    @Override
    public <V> RList<V> getList(String name, Codec codec) {
        return getClient().getList(name, codec);
    }

    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name) {
        return getClient().getListMultimap(name);
    }

    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name, Codec codec) {
        return getClient().getListMultimap(name, codec);
    }

    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name) {
        return getClient().getListMultimapCache(name);
    }

    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name, Codec codec) {
        return getClient().getListMultimapCache(name, codec);
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options) {
        return getClient().getLocalCachedMap(name, options);
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec, LocalCachedMapOptions<K, V> options) {
        return getClient().getLocalCachedMap(name, codec, options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name) {
        return getClient().getMap(name);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, MapOptions<K, V> options) {
        return getClient().getMap(name, options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec) {
        return getClient().getMap(name, codec);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
        return getClient().getMap(name, codec, options);
    }

    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name) {
        return getClient().getSetMultimap(name);
    }

    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name, Codec codec) {
        return getClient().getSetMultimap(name, codec);
    }

    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name) {
        return getClient().getSetMultimapCache(name);
    }

    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name, Codec codec) {
        return getClient().getSetMultimapCache(name, codec);
    }

    @Override
    public RSemaphore getSemaphore(String name) {
        return getClient().getSemaphore(name);
    }

    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
        return getClient().getPermitExpirableSemaphore(name);
    }

    @Override
    public RLock getLock(String name) {
        return getClient().getLock(name);
    }

    @Override
    public RLock getMultiLock(RLock... locks) {
        return getMultiLock(locks);
    }

    @Override
    public RLock getRedLock(RLock... locks) {
        return getClient().getRedLock(locks);
    }

    @Override
    public RLock getFairLock(String name) {
        return getClient().getFairLock(name);
    }

    @Override
    public RReadWriteLock getReadWriteLock(String name) {
        return getClient().getReadWriteLock(name);
    }

    @Override
    public <V> RSet<V> getSet(String name) {
        return getClient().getSet(name);
    }

    @Override
    public <V> RSet<V> getSet(String name, Codec codec) {
        return getClient().getSet(name, codec);
    }

    @Override
    public <V> RSortedSet<V> getSortedSet(String name) {
        return getClient().getSortedSet(name);
    }

    @Override
    public <V> RSortedSet<V> getSortedSet(String name, Codec codec) {
        return getClient().getSortedSet(name, codec);
    }

    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name) {
        return getClient().getScoredSortedSet(name);
    }

    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name, Codec codec) {
        return getClient().getScoredSortedSet(name, codec);
    }

    @Override
    public RLexSortedSet getLexSortedSet(String name) {
        return getClient().getLexSortedSet(name);
    }

    @Override
    public RTopic getTopic(String name) {
        return getClient().getTopic(name);
    }

    @Override
    public RTopic getTopic(String name, Codec codec) {
        return getClient().getTopic(name, codec);
    }

    @Override
    public RPatternTopic getPatternTopic(String pattern) {
        return getClient().getPatternTopic(pattern);
    }

    @Override
    public RPatternTopic getPatternTopic(String pattern, Codec codec) {
        return getClient().getPatternTopic(pattern,codec);
    }

    @Override
    public <V> RQueue<V> getQueue(String name) {
        return getClient().getQueue(name);
    }

    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name) {
        return getClient().getTransferQueue(name);
    }

    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name, Codec codec) {
        return getClient().getTransferQueue(name, codec);
    }

    @Override
    public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> destinationQueue) {
        return getClient().getDelayedQueue(destinationQueue);
    }

    @Override
    public <V> RQueue<V> getQueue(String name, Codec codec) {
        return getClient().getQueue(name, codec);
    }

    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name) {
        return getClient().getRingBuffer(name);
    }

    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name, Codec codec) {
        return getClient().getRingBuffer(name, codec);
    }

    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name) {
        return getClient().getPriorityQueue(name);
    }

    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name, Codec codec) {
        return getClient().getPriorityQueue(name, codec);
    }

    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name) {
        return getClient().getPriorityBlockingQueue(name);
    }

    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name, Codec codec) {
        return getClient().getPriorityBlockingQueue(name, codec);
    }

    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name) {
        return getClient().getPriorityBlockingDeque(name);
    }

    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name, Codec codec) {
        return getClient().getPriorityBlockingDeque(name, codec);
    }

    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name) {
        return getClient().getPriorityDeque(name);
    }

    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name, Codec codec) {
        return getClient().getPriorityDeque(name, codec);
    }

    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name) {
        return getClient().getBlockingQueue(name);
    }

    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec) {
        return getClient().getBlockingQueue(name,codec);
    }

    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name) {
        return getClient().getBoundedBlockingQueue(name);
    }

    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name, Codec codec) {
        return getClient().getBoundedBlockingQueue(name, codec);
    }

    @Override
    public <V> RDeque<V> getDeque(String name) {
        return getClient().getDeque(name);
    }

    @Override
    public <V> RDeque<V> getDeque(String name, Codec codec) {
        return getClient().getDeque(name, codec);
    }

    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name) {
        return getClient().getBlockingDeque(name);
    }

    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name, Codec codec) {
        return getClient().getBlockingDeque(name, codec);
    }

    @Override
    public RAtomicLong getAtomicLong(String name) {
        return getClient().getAtomicLong(name);
    }

    @Override
    public RAtomicDouble getAtomicDouble(String name) {
        return getClient().getAtomicDouble(name);
    }

    @Override
    public RLongAdder getLongAdder(String name) {
        return getClient().getLongAdder(name);
    }

    @Override
    public RDoubleAdder getDoubleAdder(String name) {
        return getClient().getDoubleAdder(name);
    }

    @Override
    public RCountDownLatch getCountDownLatch(String name) {
        return getClient().getCountDownLatch(name);
    }

    @Override
    public RBitSet getBitSet(String name) {
        return getClient().getBitSet(name);
    }

    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name) {
        return getClient().getBloomFilter(name);
    }

    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name, Codec codec) {
        return getClient().getBloomFilter(name, codec);
    }

    @Override
    public RScript getScript() {
        return getClient().getScript();
    }

    @Override
    public RScript getScript(Codec codec) {
        return getClient().getScript(codec);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name) {
        return getClient().getExecutorService(name);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, ExecutorOptions options) {
        return getClient().getExecutorService(name, options);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec) {
        return getClient().getExecutorService(name, codec);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec, ExecutorOptions options) {
        return getClient().getExecutorService(name, codec, options);
    }

    @Override
    public RRemoteService getRemoteService() {
        return getClient().getRemoteService();
    }

    @Override
    public RRemoteService getRemoteService(Codec codec) {
        return getClient().getRemoteService(codec);
    }

    @Override
    public RRemoteService getRemoteService(String name) {
        return getClient().getRemoteService(name);
    }

    @Override
    public RRemoteService getRemoteService(String name, Codec codec) {
        return getClient().getRemoteService(name, codec);
    }

    @Override
    public RTransaction createTransaction(TransactionOptions options) {
        return getClient().createTransaction(options);
    }

    @Override
    public RBatch createBatch(BatchOptions options) {
        return getClient().createBatch(options);
    }

    @Override
    public RBatch createBatch() {
        return getClient().createBatch();
    }

    @Override
    public RKeys getKeys() {
        return getClient().getKeys();
    }

    @Override
    public RLiveObjectService getLiveObjectService() {
        return getClient().getLiveObjectService();
    }

    @Override
    public void shutdown() {
        this.redissonClients.forEach(redissonClient -> redissonClient.shutdown());
    }

    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        this.redissonClients.forEach(redissonClient -> redissonClient.shutdown(quietPeriod, timeout, unit));
    }

    @Override
    public Config getConfig() {
        throw new RuntimeException("unsupported!");
    }

    @Override
    public <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> nodes) {
        return getClient().getRedisNodes(nodes);
    }

    @Override
    public NodesGroup<Node> getNodesGroup() {
        throw new RuntimeException("unsupported!");
    }

    @Override
    public ClusterNodesGroup getClusterNodesGroup() {
        throw new RuntimeException("unsupported!");
    }

    @Override
    public boolean isShutdown() {
        //所有都shutdown才是shutdown
        boolean allShutdown = true;
        for (RedissonClient redissonClient: redissonClients) {
            if (!redissonClient.isShutdown()) {
                allShutdown = false;
            }
        }
        return allShutdown;
    }

    @Override
    public boolean isShuttingDown() {
        //存在一个shuttingDown则返回关闭中
        return this.redissonClients.stream().filter(redissonClient -> redissonClient.isShuttingDown()).findFirst().isPresent();
    }

    @Override
    public String getId() {
        throw new RuntimeException("unsupported!");
    }
}
