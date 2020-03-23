package com.andrew.common.cache.guava;

import com.andrew.common.cache.guava.config.BaseCacheBuilderConfig;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 基础Guava Cache实现类
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Slf4j
public abstract class BaseGuavaCache<K, V> {
	protected BaseCacheBuilderConfig cacheBuilderConfig;

	// 刷新线程池
	private ExecutorService refreshPool;

	// 缓存
	private LoadingCache<K, V> cache = null;

	public BaseGuavaCache(BaseCacheBuilderConfig cacheBuilderConfig, ExecutorService refreshPool) {
		this.cacheBuilderConfig = cacheBuilderConfig;
		this.refreshPool = refreshPool;

		loadValueBeforeStarted();

		initCache();
	}

	private void initCache() {
		CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
				.concurrencyLevel(Runtime.getRuntime().availableProcessors())
				.maximumSize(cacheBuilderConfig.getMaxSize());

		if (cacheBuilderConfig.getRefreshDuration() > 0 && cacheBuilderConfig.getRefreshTimeUnit() != null) {
			cacheBuilder.refreshAfterWrite(cacheBuilderConfig.getRefreshDuration(),
					cacheBuilderConfig.getRefreshTimeUnit());
		}
		if (cacheBuilderConfig.getExpireDuration() > 0 && cacheBuilderConfig.getExpireTimeUnit() != null) {
			cacheBuilder
					.expireAfterWrite(cacheBuilderConfig.getExpireDuration(), cacheBuilderConfig.getExpireTimeUnit());
		}

		cache = cacheBuilder.build(CacheLoader.asyncReloading(new CacheLoader<K, V>() {
			@Override
			public V load(K key) {
				return getValueWhenExpired(key);
			}
		}, refreshPool));
	}

	/**
	 * 被构造函数调用看，可用于对配置等进行检查
	 */
	public abstract void loadValueBeforeStarted();

	/**
	 * key对应的value过期或不存在时调用
	 *
	 * @param key
	 * @return
	 * @throws RuntimeException
	 */
	protected abstract V getValueWhenExpired(K key);

	public V getValue(K key) throws ExecutionException {
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

	public static void main(String[] args) {
		new BaseGuavaCache<String, String>(new BaseCacheBuilderConfig(10, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, 50),
				Executors.newFixedThreadPool(10)) {
			@Override
			public void loadValueBeforeStarted() {
				// cache实例化的时候触发操作，例如检查配置等
			}

			@Override
			protected String getValueWhenExpired(String key) {
				// TODO 从数据库等获取数据
				return null;
			}
		};
	}
}
