package com.andrew.common.http.okhttp;

import com.andrew.common.text.StringUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * OkHttp封装类
 *
 * @Author Andrew
 * @Date 2019-06-13 16:41
 */
@Slf4j
public class WebHttpClientBuilder {
	/* OkHttp */
	private OkHttpClient.Builder builder = new OkHttpClient.Builder();

	/** 最大并发请求数*/
	private int maxRequests;
	/** 每个主机最大请求数*/
	private int maxRequestsPerHost;

	private static final String TIMEOUT_THRESHOLD_CHECK_INFO = "{} invalid. must > 0";

	private static final String TIMEOUT_UNIT_CHECK_INFO = "TimeUnit can not be null";

	/** OkHttp connection超时时间2000毫秒*/
	public static final Integer DEFAULT_CONNECTION_TIMEOUT = 2000;

	/** OkHttp read超时时间5000毫秒*/
	public static final Integer DEFAULT_READ_TIMEOUT = 5000;

	/** OkHttp write超时时间5000毫秒*/
	public static final Integer DEFAULT_WRITE_TIMEOUT = 5000;

	public WebHttpClientBuilder() {
		// 设置默认值
		// 建立TCP连接的超时时间
		builder.connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
				// 读超时时间
				.readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS)
				// 写超时时间
				.writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	// ------------ 创建WebProxyHttpClient实例
	public WebHttpClient build() {
		return new WebHttpClient(this.builder, maxRequests, maxRequestsPerHost);
	}

	//================超时时间设置方法================

	/**
	 * 设置连接超时时间
	 * @param connectTimeout
	 * @param timeUnit
	 * @return
	 */
	public WebHttpClientBuilder connectTimeout(int connectTimeout, TimeUnit timeUnit) {
		if (connectTimeout <= 0) {
			log.error(StringUtil.format(TIMEOUT_THRESHOLD_CHECK_INFO, "connectTimeout"));
			return this;
		}

		if (timeUnit == null) {
			log.error(TIMEOUT_UNIT_CHECK_INFO);
			return this;
		}
		this.builder.connectTimeout(connectTimeout, timeUnit);
		return this;
	}

	/**
	 * 设置读超时时间
	 * @param readTimeout
	 * @param timeUnit
	 * @return
	 */
	public WebHttpClientBuilder readTimeout(int readTimeout, TimeUnit timeUnit) {
		if (readTimeout <= 0) {
			log.error(StringUtil.format(TIMEOUT_THRESHOLD_CHECK_INFO, "readTimeout"));
			return this;
		}

		if (timeUnit == null) {
			log.error(TIMEOUT_UNIT_CHECK_INFO);
			return this;
		}
		this.builder.readTimeout(readTimeout, timeUnit);
		return this;
	}

	/**
	 * 设置写入超时时间
	 * @param writeTimeout
	 * @param timeUnit
	 * @return
	 */
	public WebHttpClientBuilder writeTimeout(int writeTimeout, TimeUnit timeUnit) {
		if (writeTimeout <= 0) {
			log.error(StringUtil.format(TIMEOUT_THRESHOLD_CHECK_INFO, "writeTimeout"));
			return this;
		}

		if (timeUnit == null) {
			log.error(TIMEOUT_UNIT_CHECK_INFO);
			return this;
		}
		this.builder.writeTimeout(writeTimeout, timeUnit);
		return this;
	}

	/**
	 * 设置最大连接数
	 * @param maxRequests
	 * @param maxRequestsPerHost
	 * @return
	 */
	public WebHttpClientBuilder maxRequests(int maxRequests, int maxRequestsPerHost) {
		this.maxRequests = maxRequests;
		this.maxRequestsPerHost = maxRequestsPerHost;
		return this;
	}
}
