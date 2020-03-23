package com.andrew.common.http.okhttp.callback;

import com.andrew.common.http.okhttp.datahandler.DataHandler;
import okhttp3.Call;
import okhttp3.Response;

import java.util.Map;

/**
 * 异步请求的回调接口
 */
public interface Callback<T> {
	/**
	 * 在请求前调用
	 */
	void onBefore(String url, String body, Map<String, String> headerMap);

	/**
	 * 请求失败调用
	 */
	void onError(Call call, Exception exception);

	/**
	 * 请求完成调用
	 */
	void onComplete(Response response);

	/**
	 * 数据处理器,用于解析转换响应结果
	 */
	DataHandler<T> getDataHandler();

	/**
	 * 根据数据处理器得到处理结果,调用者直接使用处理后的数据
	 */
	void onSuccess(T data);
}
