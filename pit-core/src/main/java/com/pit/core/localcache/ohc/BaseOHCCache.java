package com.pit.core.localcache.ohc;

import com.pit.core.json.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.caffinitas.ohc.CacheSerializer;
import org.caffinitas.ohc.Eviction;
import org.caffinitas.ohc.OHCache;
import org.caffinitas.ohc.OHCacheBuilder;

/**
 * @Description 基础OHC(off heap cache)实现类
 * @Author gy
 * @Date 2020-07-01
 */
@Slf4j
public abstract class BaseOHCCache<K, V> {
	private OHCache<K, V> cache;

	private CacheSerializer<K> keySerializer;
	private CacheSerializer<V> valueSerializer;

	private OHCConfig config;

	public BaseOHCCache(OHCConfig config,
						CacheSerializer<K> keySerializer,
						CacheSerializer<V> valueSerializer) {
		this.config = config;
		this.keySerializer = keySerializer;
		this.valueSerializer = valueSerializer;

		beforeStart();
		initCache();
	}

	private void initCache() {
		log.info("{} cache config is {}", getName(), GsonUtils.toJson(config));

		OHCacheBuilder builder = OHCacheBuilder.<K, V>newBuilder()
				.keySerializer(keySerializer)
				.valueSerializer(valueSerializer)
				// "LRU" 最旧的（最近最少使用的）条目被逐出
				// "W_TINY_LFU" 使用频率较低的条目被逐出，以便为新条目腾出空间
				.eviction(Eviction.W_TINY_LFU)
				.throwOOME(true);

		if (config.getSegmentCount() > 0) {
			// number of segments (must be a power of 2), defaults to number-of-cores * 2
			builder.segmentCount(config.getSegmentCount());
		}

		if (config.getHashTableSize() > 0) {
			// hash table size (must be a power of 2), defaults to 8192
			builder.hashTableSize(config.getHashTableSize());
		}

		if (config.getCapacity() > 0) {
			// Capacity of the cache in bytes
			builder.capacity(config.getCapacity());
		}

		if (null != config.getTimeouts()) {
			// 是否开启缓存过期
			builder.timeouts(config.getTimeouts());
		}

		if (config.getDefaultTTLmillis() > 0) {
			// 全局默认过期时间，毫秒
			builder.defaultTTLmillis(config.getDefaultTTLmillis());
		}

		if (config.getTimeoutsSlots() > 0) {
			// The number of timeouts slots for each segment - compare with hashed wheel timer.
			// 每个段的超时插槽数
			builder.timeoutsSlots(config.getTimeoutsSlots());
		}

		if (config.getTimeoutsPrecision() > 0) {
			// The amount of time in milliseconds for each timeouts-slot.
			// 每个timeouts-slot的时间量（以毫秒为单位）
			builder.timeoutsPrecision(config.getTimeoutsPrecision());
		}

		if (null != config.getExecutorService()) {
			builder.executorService(config.getExecutorService());
		}

		cache = builder.build();
	}

	/**
	 * 被构造函数调用看，可用于对配置等进行检查
	 */
	protected void beforeStart() {
	}

	public V get(K key) {
		return getCache().get(key);
	}

	public OHCache<K, V> getCache() {
		return cache;
	}

	public void clearAll() {
		this.getCache().clear();
	}

	protected abstract String getName();


}
