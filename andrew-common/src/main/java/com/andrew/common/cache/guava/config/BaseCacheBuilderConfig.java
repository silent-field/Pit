package com.andrew.common.cache.guava.config;

import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class BaseCacheBuilderConfig implements ICacheBuilderConfig {
	// 缓存刷新频率
	private Integer refreshDuration;
	// 缓存刷新时间格式
	private TimeUnit refreshTimeUnit;
	// 缓存过期时间
	private Integer expireDuration;
	// 缓存刷新时间格式
	private TimeUnit expireTimeUnit;
	// 缓存最大容量
	private Integer maxSize;

	public BaseCacheBuilderConfig(Integer refreshDuration, TimeUnit refreshTimeUnit, Integer expireDuration,
								  TimeUnit expireTimeUnit, Integer maxSize) {
		this.refreshDuration = refreshDuration;
		this.refreshTimeUnit = refreshTimeUnit;
		this.expireDuration = expireDuration;
		this.expireTimeUnit = expireTimeUnit;
		this.maxSize = maxSize;
	}

}
