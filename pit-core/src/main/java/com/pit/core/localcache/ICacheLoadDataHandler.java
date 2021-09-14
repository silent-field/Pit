package com.pit.core.localcache;

/**
 * 缓存回源操作接口
 *
 * @Author gy
 * @Date 2020-03-20
 */
public interface ICacheLoadDataHandler<K, V> {
	/**
	 * 加载数据
	 * @param key
	 * @return
	 */
	V loadData(K key);
}
