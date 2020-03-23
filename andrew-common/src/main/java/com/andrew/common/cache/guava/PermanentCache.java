package com.andrew.common.cache.guava;

import com.andrew.common.cache.guava.config.BaseCacheBuilderConfig;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 无过期时间的Guava Cache
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Slf4j
public final class PermanentCache<K, V> extends BaseGuavaCache<K, V> {
	private ICacheLoadDataHandler<K, V> loadDataHandler;

	public PermanentCache(BaseCacheBuilderConfig cacheBuilderConfig, ICacheLoadDataHandler<K, V> loadDataHandler) {
		this(cacheBuilderConfig, MoreExecutors
						.listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())),
				loadDataHandler);
	}

	public PermanentCache(BaseCacheBuilderConfig cacheBuilderConfig, ExecutorService refreshPool,
						  ICacheLoadDataHandler<K, V> loadDataHandler) {
		super(cacheBuilderConfig, refreshPool);
		this.loadDataHandler = loadDataHandler;
		Validate.isTrue(null != loadDataHandler, "loadDataHandler is null");
	}

	/**
	 * 检查配置项
	 */
	private void checkAndSetConfig() {
		Validate.notNull(cacheBuilderConfig, "cacheBuilderConfig is null");
		Validate.notNull(cacheBuilderConfig.getRefreshDuration(), "cacheBuilderConfig.refreshDuration is null");
		Validate.isTrue(Integer.valueOf(0).compareTo(cacheBuilderConfig.getRefreshDuration()) > 0, "cacheBuilderConfig.refreshDuration is invalid, must > 0");
		Validate.notNull(cacheBuilderConfig.getRefreshTimeUnit(), "cacheBuilderConfig.refreshTimeunit is null");
		Validate.notNull(cacheBuilderConfig.getMaxSize(), "cacheBuilderConfig.maxsize is null");
		Validate.isTrue(Integer.valueOf(0).compareTo(cacheBuilderConfig.getMaxSize()) > 0,
				"cacheBuilderConfig.maxsize is invalid, must > 0");

		cacheBuilderConfig.setExpireDuration(-1);
		cacheBuilderConfig.setExpireTimeUnit(null);
	}

	@Override
	public void loadValueBeforeStarted() {
		checkAndSetConfig();
	}

	protected V getValueWhenExpired(K key) {
		return loadDataHandler.loadData(key);
	}
}
