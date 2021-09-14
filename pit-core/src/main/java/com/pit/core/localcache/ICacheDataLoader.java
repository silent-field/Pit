package com.pit.core.localcache;

/**
 * @Description 缓存回源操作接口
 *
 * @Author gy
 * @Date 2019-06-12 17:08
 */
public interface ICacheDataLoader<K, V> {
    /**
     * 加载数据
     * @param key
     * @return
     */
    V loadData(K key);
}
