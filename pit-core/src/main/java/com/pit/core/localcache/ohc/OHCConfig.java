package com.pit.core.localcache.ohc;

import lombok.Builder;
import lombok.Data;
import org.caffinitas.ohc.CacheLoader;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @Description 基础 OHC Cache 配置类
 * @Author gy
 * @Date 2020-07-01
 */
@Data
@Builder
public class OHCConfig {
    /**
     * number of segments (must be a power of 2), defaults to number-of-cores * 2
     */
    private int segmentCount;

    /**
     * Initial size of each segment's hash table, defaults 8192
     */
    private int hashTableSize;

    /**
     * Capacity of the cache in bytes
     * capacity 代表的是缓存最大字节数，而不是缓存对象数量
     */
    private long capacity;

    /**
     * 是否开启过期
     */
    private Boolean timeouts;

    /**
     * The number of timeouts slots for each segment - compare with hashed wheel timer.
     */
    private int timeoutsSlots;

    /**
     * The amount of time in milliseconds for each timeouts-slot.
     */
    private int timeoutsPrecision;

    /**
     * If set to a value {@code > 0}, implementations supporting TTLs will tag all entries with the given TTL in milliseconds
     */
    private long defaultTTLmillis;

    /**
     * Executor service required for get operations using a cache loader.
     * E.g. {@link org.caffinitas.ohc.OHCache#getWithLoaderAsync(Object, CacheLoader)}
     */
    private transient ScheduledExecutorService executorService;
}
