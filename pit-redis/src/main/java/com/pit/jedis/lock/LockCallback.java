package com.pit.jedis.lock;

/**
 * redis 锁 回调函数
 *
 * @param <T>
 * @author gy
 */
public interface LockCallback<T> {
    /**
     * 回调执行
     *
     * @return
     */
    T exec();
}