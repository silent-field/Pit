package com.andrew.common.loadbalance.config;

/**
 * @Author Andrew
 * @Date 2019-06-17 17:35
 */
public class Const {
	private Const() {
	}

	/**
	 * 缓存刷新时间10秒
	 */
	public static final Integer SERVICE_META_REFRESH_TIME = 10;

	/**
	 * 缓存最大数量50
	 */
	public static final Integer SERVICE_META_MAXSIZE = 50;

	/**
	 * 缓存刷新线程池数量5
	 */
	public static final Integer REFRESH_POOL_SIZE = 5;
}
