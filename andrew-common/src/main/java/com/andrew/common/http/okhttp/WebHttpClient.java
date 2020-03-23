package com.andrew.common.http.okhttp;

import com.alibaba.fastjson.JSONObject;
import com.andrew.common.http.okhttp.callback.CallbackAdapter;
import com.andrew.common.http.okhttp.datahandler.DataHandler;
import com.andrew.common.http.okhttp.datahandler.JSONDataHandler;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp封装类
 *
 * @Author Andrew
 * @Date 2019-06-13 16:41
 */
@Slf4j
public class WebHttpClient {
	/* OkHttp */
	private OkHttpClient okHttpClient;

	public WebHttpClient(OkHttpClient.Builder builder, int maxRequests, int maxRequestsPerHost) {
		this.okHttpClient = builder.build();
		if (maxRequests > 0) {
			this.okHttpClient.dispatcher().setMaxRequests(maxRequests);
		}

		if (maxRequestsPerHost > 0) {
			this.okHttpClient.dispatcher().setMaxRequestsPerHost(maxRequestsPerHost);
		}
	}

	// --------------- 网络请求
	public <T> T post(String url, String jsonBody, Map<String, String> headers, DataHandler<T> dataHandler)
			throws IOException {
		return syncReq(Builder.newPostReq(url, jsonBody, headers), dataHandler);
	}

	public JSONObject post(String url, String jsonBody, Map<String, String> headers) throws IOException {
		return syncReq(Builder.newPostReq(url, jsonBody, headers), new JSONDataHandler());
	}

	public void asyncPost(String url, String jsonBody, Map<String, String> headers,
						  com.andrew.common.http.okhttp.callback.Callback callback) {
		asyncReq(Builder.newPostReq(url, jsonBody, headers), callback);
	}

	private <T> T syncReq(Builder.OkHttpRequestWrapper okHttpRequestWrapper, DataHandler<T> dataHandler)
			throws IOException {
		try (Response response = okHttpClient.newCall(okHttpRequestWrapper.getOkHttpRequest()).execute()) {
			if (!response.isSuccessful()) {
				log.error("Unexpected code " + response);
				throw new IOException("Unexpected code " + response);
			}

			return dataHandler.handle(response);
		}
	}

	private void asyncReq(Builder.OkHttpRequestWrapper okHttpRequestWrapper,
						  com.andrew.common.http.okhttp.callback.Callback callback) {
		if (callback == null) {
			// 空实现
			callback = new CallbackAdapter() {
			};
		}

		final com.andrew.common.http.okhttp.callback.Callback realCb = callback;
		realCb.onBefore(okHttpRequestWrapper.getOriginUrl(), okHttpRequestWrapper.getOriginBody(),
				okHttpRequestWrapper.getHeaders());

		okHttpClient.newCall(okHttpRequestWrapper.getOkHttpRequest()).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				realCb.onError(call, e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				realCb.onComplete(response);

				if (response.code() == 200) {
					try {
						// 如果使用了CallBackAdapter，那么realCb.getDataHandler()为null
						if (realCb.getDataHandler() != null) {
							realCb.onSuccess(realCb.getDataHandler().handle(response));
						}
					} finally {
						response.close();
					}
				} else {
					realCb.onError(call, null);
				}
			}
		});
	}

	public void destroy() {
		MoreExecutors
				.shutdownAndAwaitTermination(okHttpClient.dispatcher().executorService(), 3000, TimeUnit.MILLISECONDS);
	}
}
