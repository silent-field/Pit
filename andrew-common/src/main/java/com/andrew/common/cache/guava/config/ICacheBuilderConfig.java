package com.andrew.common.cache.guava.config;

import java.util.concurrent.TimeUnit;

public interface ICacheBuilderConfig {
	/**
	 * 缓存刷新频率
	 * @return
	 */
	Integer getRefreshDuration();

	/**
	 * 缓存刷新时间格式
	 * @return
	 */
	TimeUnit getRefreshTimeUnit();

	/**
	 * 缓存过期时间
	 * @return
	 */
	Integer getExpireDuration();

	/**
	 * 缓存刷新时间格式
	 * @return
	 */
	TimeUnit getExpireTimeUnit();

	/**
	 * 缓存最大容量
	 * @return
	 */
	Integer getMaxSize();
}
