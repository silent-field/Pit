package com.pit.core.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存系统时间，避免每次调用System.currentTimeMillis()消耗系统资源。适用于高并发、对时间精度要求不高的场景
 *
 * @author gy
 * @date 2020/3/20
 */
public class CachingSystemTimer {
	private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	/**
	 * 更新间隔
	 */
	private long interval;

	/**
	 * 时间
	 */
	private static volatile long time = System.currentTimeMillis();

	public CachingSystemTimer() {
		// 1s更新一次
		this(1000);
	}

	public CachingSystemTimer(long interval) {
		this.interval = interval;
		init();
	}

	private static class TimerFormatTicker implements Runnable {
		@Override
		public void run() {
			time = System.currentTimeMillis();
		}
	}

	public long getTime() {
		return time;
	}

	private void init() {
		time = System.currentTimeMillis();
		executor.scheduleAtFixedRate(new TimerFormatTicker(), interval, interval, TimeUnit.MILLISECONDS);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				executor.shutdown();
			}
		});
	}
}