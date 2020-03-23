package com.andrew.common.http.async;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;

@Slf4j
public class AndrewAsyncHttpClient {
	private static AsyncHttpClient asyncHttpClient;

	private static synchronized void init(int connectTTL, int connectTimeout, int maxConnect, int maxConnectPerHost) throws Exception {
		if (null != asyncHttpClient) {
			return;
		}
		DefaultAsyncHttpClientConfig.Builder asynBuilder = Dsl.config();
		asynBuilder.setStrict302Handling(false);
		asynBuilder.setConnectionTtl(connectTTL);
		asynBuilder.setConnectTimeout(connectTimeout);
		asynBuilder.setMaxConnections(maxConnect);
		asynBuilder.setMaxConnectionsPerHost(maxConnectPerHost);
		asyncHttpClient = Dsl.asyncHttpClient(asynBuilder);
	}

	public static AsyncHttpClient asyncHttpClient(int connectTTL, int connectTimeout, int maxConnect, int maxConnectPerHost) {
		try {
			// asyncHttpClient
			if (asyncHttpClient == null) {
				init(connectTTL, connectTimeout, maxConnect, maxConnectPerHost);
			}
			return asyncHttpClient;
		} catch (Exception e) {
			log.error("ECOAsyncHttpClient 初始化异常", e);
		}
		return null;
	}
}
