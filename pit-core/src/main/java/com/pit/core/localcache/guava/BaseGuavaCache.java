package com.pit.core.localcache.guava;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pit.core.json.GsonUtils;
import com.pit.core.localcache.ICacheDataLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @Description 基础Guava Cache实现类
 * @Author gy
 * @Date 2020-07-01
 */
@Slf4j
public abstract class BaseGuavaCache<K, V> {
    protected GuavaCacheConfig cacheBuilderConfig;

    /**
     * 刷新线程池
     */
    private ExecutorService refreshPool;

    private LoadingCache<K, V> cache;

    private ICacheDataLoader<K, V> dataHandler;

    public BaseGuavaCache(GuavaCacheConfig cacheBuilderConfig,
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
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .maximumSize(cacheBuilderConfig.getMaxSize());

        if (cacheBuilderConfig.getExpireAfterWriteDuration() > 0 && cacheBuilderConfig.getExpireAfterWriteTimeUnit() != null) {
            cacheBuilder
                    .expireAfterWrite(cacheBuilderConfig.getExpireAfterWriteDuration(), cacheBuilderConfig.getExpireAfterWriteTimeUnit());
        }

        if (cacheBuilderConfig.getExpireAfterAccessDuration() > 0 && cacheBuilderConfig.getExpireAfterAccessTimeUnit() != null) {
            cacheBuilder
                    .expireAfterAccess(cacheBuilderConfig.getExpireAfterAccessDuration(), cacheBuilderConfig.getExpireAfterAccessTimeUnit());
        }

        if (cacheBuilderConfig.getRefreshDuration() > 0 && cacheBuilderConfig.getRefreshTimeUnit() != null) {
            cacheBuilder.refreshAfterWrite(cacheBuilderConfig.getRefreshDuration(),
                    cacheBuilderConfig.getRefreshTimeUnit());
        }

        if (cacheBuilderConfig.getConcurrencyLevel() > 0) {
            cacheBuilder.concurrencyLevel(cacheBuilderConfig.getConcurrencyLevel());
        }

        if (cacheBuilderConfig.isDisplayCacheStats()) {
            cacheBuilder.recordStats();
        }

        /**
         * 使用CacheLoader.asyncReloading，当触发回源时，触发回源的线程也不会被阻塞，回源任务会交由线程池处理，触发回源的线程会返回oldValue
         */
        cache = cacheBuilder.build(CacheLoader.asyncReloading(new CacheLoader<K, V>() {
            @Override
            public V load(K key) {
                return getValueWhenExpired(key);
            }
        }, refreshPool));

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

    public V get(K key) throws ExecutionException {
        return getCache().get(key);
    }

    /**
     * 初始化并返回{@linkplain LoadingCache}实例
     *
     * @return
     */
    private LoadingCache<K, V> getCache() {
        return cache;
    }

    public void clearAll() {
        this.getCache().invalidateAll();
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
                cache.cleanUp();
                log.info("{} print stats : {}", getName(), cache.stats());
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
