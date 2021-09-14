package com.pit.core.time;

import com.pit.core.thread.PitThreadFactory;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author gy
 * @date 2020/3/20
 * <p>
 * 缓存时间，日期工具类
 */
public class CachingSystemTimer2 {
    public static final FastDateFormat ON_DATE_FORMAT = DateFormatUtil.ISO_ON_DATE_FORMAT;
    public static final FastDateFormat ON_SECOND_FORMAT = DateFormatUtil.DEFAULT_ON_SECOND_FORMAT;
    public static final FastDateFormat ON_MILlSECOND_FORMAT = DateFormatUtil.DEFAULT_FORMAT;
    private static volatile int count = 0;
    private static volatile long old = 0L;
    private static volatile long now = 0L;
    private static volatile String dateNow = null;
    private static volatile String secondNow = null;
    private static volatile String msNow = null;
    private static volatile AtomicLong idx = new AtomicLong(0L);

    public CachingSystemTimer2() {
    }

    public static long getNow() {
        if (0L == now) {
            init();
        }

        return now;
    }

    public static String getDateNow() {
        if (null == dateNow) {
            init();
        }

        return dateNow;
    }

    public static String getSecondNow() {
        if (null == secondNow) {
            init();
        }

        return secondNow;
    }

    public static String getMsNow() {
        if (null == msNow) {
            init();
        }

        return msNow;
    }

    public static long getNowIndex() {
        return idx.getAndAdd(2L);
    }

    public static long getResetCount() {
        return (long) count;
    }

    public static Date stringToDate(String dateStr) throws ParseException {
        return ON_DATE_FORMAT.parse(dateStr);
    }

    private static synchronized void init() {
        if (now <= 0L) {
            ScheduledExecutorService scheduleThreadPool =
                    Executors.newScheduledThreadPool(1, new PitThreadFactory("windrunne_cache_time"));
            scheduleThreadPool.scheduleWithFixedDelay(() -> {
                long nowTmp = System.currentTimeMillis();
                if (nowTmp != now) {
                    if (old > nowTmp) {
                        count = Math.max(++count, 9);
                    }

                    old = now;
                    now = nowTmp;
                    if (idx.get() % 2L == 1L) {
                        idx.set(0L);
                    } else {
                        idx.set(1L);
                    }

                    dateNow = ON_DATE_FORMAT.format(now);
                    secondNow = ON_SECOND_FORMAT.format(now);
                    msNow = ON_MILlSECOND_FORMAT.format(now);
                }
            }, 0L, 1L, TimeUnit.MILLISECONDS);
            now = System.currentTimeMillis();
        }
    }
}