package com.pit.core.localcache.guava;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @Description 基础 Guava Cache 配置类
 * @Author gy
 * @Date 2020-07-01
 */
@Data
@Builder
public class GuavaCacheConfig {
	/**
	 * 缓存刷新频率
	 */
	private long refreshDuration;
	/**
	 * 缓存刷新时间格式
	 */
	private TimeUnit refreshTimeUnit;
	/**
	 * 缓存写后过期时间
	 */
	private long expireAfterWriteDuration;
	/**
	 * 缓存写后刷新时间格式
	 */
	private TimeUnit expireAfterWriteTimeUnit;
	/**
	 * 缓存访问后过期时间
	 */
	private long expireAfterAccessDuration;
	/**
	 * 缓存访问后刷新时间格式
	 */
	private TimeUnit expireAfterAccessTimeUnit;
	/**
	 *
	 */
	private long maxSize;
	/**
	 * 并发更新操作数
	 */
	private int concurrencyLevel;

	// -------------- 指标相关
	/**
	 * 是否显示缓存指标
	 */
	private boolean displayCacheStats;
	private long displayCacheStatsInitialDelay;
	private long displayCacheStatsDelay;
	private TimeUnit displayCacheStatsTimeUnit;
}
