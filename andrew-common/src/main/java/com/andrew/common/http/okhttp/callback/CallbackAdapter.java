package com.andrew.common.http.okhttp.callback;

import com.andrew.common.http.okhttp.datahandler.DataHandler;
import okhttp3.Call;
import okhttp3.Response;

import java.util.Map;

/**
 * {@linkplain Callback}的空实现
 *
 * @Author Andrew
 * @Date 2019-06-13 17:58
 */
public abstract class CallbackAdapter<T> implements Callback<T> {
	@Override
	public void onBefore(String url, String body, Map<String, String> headerMap) {

	}

	@Override
	public void onError(Call call, Exception exception) {

	}

	@Override
	public void onComplete(Response response) {

	}

	@Override
	public DataHandler<T> getDataHandler() {
		return null;
	}

	@Override
	public void onSuccess(T data) {

	}
}
