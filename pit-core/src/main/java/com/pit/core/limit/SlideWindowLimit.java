package com.pit.core.limit;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author gy
 *
 * 滑动窗口限流实现。
 * 与计数限流对比，能保证不会出现临界问题。
 * 临界问题说明：
 * 假设QPS限流100。第1秒的后500ms内有100个请求，第2秒的前500ms有100个请求，那么在500ms-1500ms这1秒内其实最大QPS为200
 */
public class SlideWindowLimit {
    private final Object lock = new Object();
    /**
     * 接受请求窗口
     */
    private Long[] accessWindow;
    /**
     * 窗口大小
     */
    private int limit;
    /**
     * 指针位置
     */
    private int curPosition;
    /**
     * 时间间隔
     */
    private long period;

    /**
     * @param limit    限制次数
     * @param period   时间间隔
     * @param timeUnit 间隔类型
     */
    public SlideWindowLimit(int limit, int period, TimeUnit timeUnit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Illegal limit Capacity: " + limit);
        }

        if (period < 0) {
            throw new IllegalArgumentException("Illegal period Capacity: " + limit);
        }
        curPosition = 0;
        this.period = timeUnit.toMillis(period);
        this.limit = limit;
        accessWindow = new Long[limit];
        Arrays.fill(accessWindow, 0L);
    }

    public boolean grant() {
        long curTime = System.currentTimeMillis();
        synchronized (lock) {
            if (curTime >= period + accessWindow[curPosition]) {
                accessWindow[curPosition++] = curTime;
                curPosition = curPosition % limit;
                return true;
            } else {
                return false;
            }
        }
    }
}