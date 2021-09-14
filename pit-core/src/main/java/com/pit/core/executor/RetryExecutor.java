package com.pit.core.executor;

/**
 * 带重试的执行接口
 *
 * @author gy
 * @date 2020/3/20
 */
public interface RetryExecutor<T, R> {
    /**
     * 执行操作
     *
     * @param resource
     * @return
     */
    public T exec(R resource);
}
