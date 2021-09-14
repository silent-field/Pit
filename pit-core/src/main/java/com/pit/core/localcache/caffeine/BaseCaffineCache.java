package com.pit.core.localcache.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.pit.core.json.GsonUtils;
import com.pit.core.localcache.ICacheDataLoader;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @Description 基础 Caffine Cache实现类
 * @Author gy
 * @Date 2020-07-01
 */
@Slf4j
public abstract class BaseCaffineCache<K, V> {
    protected CaffineCacheConfig cacheBuilderConfig;

    // 刷新线程池
    private ExecutorService refreshPool;

    // 缓存
    private AsyncLoadingCache<K, V> cache;

    private ICacheDataLoader<K, V> dataHandler;

    public BaseCaffineCache(CaffineCacheConfig cacheBuilderConfig,
                            ExecutorService refreshPool,
                            ICacheDataLoader<K, V> dataHandler) {
        this.cacheBuilderConfig = cacheBuilderConfig;
        this.refreshPool = refreshPool;
        this.dataHandler = dataHandler;

        beforeStart();

        initCache();
    }

    private void initCache() {
        log.info("{} cache config is {}", getName(), GsonUtils.toJson(cacheBuilderConfig));

        /**
         * expireAfterWrite 是在指定项在一定时间内没有创建/覆盖时，会移除该key，下次取的时候从loading中取
         * expireAfterAccess 是指定项在一定时间内没有读写，会移除该key，下次取的时候从loading中取
         * refreshAfterWrite 是在指定时间内没有被创建/覆盖，则指定时间过后，再次访问时，会去刷新该缓存，在新值没有到来之前，始终返回旧值
         * 					跟expire的区别是，指定时间过后，expire是remove该key，下次访问是同步去获取返回新值；
         * 					而refresh则是指定时间后，不会remove该key，下次访问会触发刷新，新值没有回来时返回旧值
         */
        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
                .maximumSize(cacheBuilderConfig.getMaxSize());

        if (cacheBuilderConfig.getExpireAfterWriteDuration() > 0 && cacheBuilderConfig.getExpireAfterWriteTimeUnit() != null) {
            cacheBuilder
                    .expireAfterWrite(cacheBuilderConfig.getExpireAfterWriteDuration(), cacheBuilderConfig.getExpireAfterWriteTimeUnit());
        }

        if (cacheBuilderConfig.getExpireAfterAccessDuration() > 0 && cacheBuilderConfig.getExpireAfterAccessTimeUnit() != null) {
            cacheBuilder
                    .expireAfterWrite(cacheBuilderConfig.getExpireAfterAccessDuration(), cacheBuilderConfig.getExpireAfterAccessTimeUnit());
        }

        if (cacheBuilderConfig.getRefreshDuration() > 0 && cacheBuilderConfig.getRefreshTimeUnit() != null) {
            cacheBuilder.refreshAfterWrite(cacheBuilderConfig.getRefreshDuration(),
                    cacheBuilderConfig.getRefreshTimeUnit());
        }

        if (cacheBuilderConfig.isDisplayCacheStats()) {
            cacheBuilder.recordStats();
        }

        if (null != refreshPool) {
            cacheBuilder.executor(refreshPool);
        }

        /**
         * 使用CacheLoader.asyncReloading，当触发回源时，触发回源的线程也不会被阻塞，回源任务会交由线程池处理，触发回源的线程会返回oldValue
         */
        cache = cacheBuilder.buildAsync(new AsyncCacheLoader<K, V>() {
            @Override
            public @NonNull CompletableFuture<V> asyncLoad(@NonNull K key, @NonNull Executor executor) {
                return CompletableFuture.supplyAsync(new Supplier<V>() {
                    @Override
                    public V get() {
                        return getValueWhenExpired(key);
                    }
                });
            }
        });

        if (cacheBuilderConfig.isDisplayCacheStats()) {
            displayCacheStats();
        }
    }

    /**
     * 被构造函数调用看，可用于对配置等进行检查
     */
    protected void beforeStart() {
    }

    /**
     * key对应的value过期或不存在时调用
     *
     * @param key
     * @return
     * @throws RuntimeException
     */
    protected V getValueWhenExpired(K key) {
        return dataHandler.loadData(key);
    }

    public CompletableFuture<V> get(K key) throws ExecutionException {
        return getCache().get(key);
    }

    public CompletableFuture<Map<K, V>> batchGet(List<K> keys) throws ExecutionException {
        return getCache().getAll(keys);
    }

    /**
     * 初始化并返回{@linkplain LoadingCache}实例
     *
     * @return
     */
    private AsyncLoadingCache<K, V> getCache() {
        return cache;
    }

    private void displayCacheStats() {
        long displayCacheStatsInitialDelay = 10;
        long displayCacheStatsDelay = 30;
        TimeUnit displayCacheStatsTimeUnit = TimeUnit.SECONDS;

        if (cacheBuilderConfig.getDisplayCacheStatsInitialDelay() > 0L
                && cacheBuilderConfig.getDisplayCacheStatsDelay() > 0L
                && null != cacheBuilderConfig.getDisplayCacheStatsTimeUnit()) {
            displayCacheStatsInitialDelay = cacheBuilderConfig.getDisplayCacheStatsInitialDelay();
            displayCacheStatsDelay = cacheBuilderConfig.getDisplayCacheStatsDelay();
            displayCacheStatsTimeUnit = cacheBuilderConfig.getDisplayCacheStatsTimeUnit();
        }

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                log.info("{} print stats : {}", getName(), cache.synchronous().stats());
            } catch (Exception e) {
                log.error(getName() + " print stats error", e);
            }
        }, displayCacheStatsInitialDelay, displayCacheStatsDelay, displayCacheStatsTimeUnit);
    }

    /**
     * 缓存名
     *
     * @return
     */
    protected abstract String getName();
}
