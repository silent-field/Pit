package com.andrew.common.limit;

import java.util.concurrent.TimeUnit;

/**
 * 简单计数限流实现。
 * 与滑动窗口限流对比，只能保证每个时间间隔内限流，无法保证临界问题。
 * 临界问题说明：
 * 假设QPS限流100。第1秒的后500ms内有100个请求，第2秒的前500ms有100个请求，那么在500ms-1500ms这1秒内其实最大QPS为200
 */
public class SimpleCountLimitGrantWhitTime {
	/**
	 * 起始点
	 */
	private long startPoint;

	/**
	 * 当前请求数
	 */
	private int count = 0;

	/**
	 * 上限
	 */
	private int limit;

	/**
	 * 时间间隔
	 */
	private long period;

	private final Object lock = new Object();

	/**
	 * @param limit    限制次数
	 * @param period   时间间隔
	 * @param timeUnit 间隔类型
	 */
	public SimpleCountLimitGrantWhitTime(int limit, int period, TimeUnit timeUnit) {
		this.startPoint = System.currentTimeMillis();
		this.period = timeUnit.toMillis(period);
		this.limit = limit;
	}


	public boolean grant(long curTime) {
		synchronized (lock) {
			count++;
			if (count > limit) {
				if (curTime - startPoint > period) {
					startPoint = curTime;
					count = 1;
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		SimpleCountLimitGrantWhitTime logLimit = new SimpleCountLimitGrantWhitTime(1, 30, TimeUnit.SECONDS);

		for(int i = 0; i < 100; i++){
			boolean re = logLimit.grant(System.currentTimeMillis());
			System.out.println(re);
			Thread.sleep(400);
		}
	}
}