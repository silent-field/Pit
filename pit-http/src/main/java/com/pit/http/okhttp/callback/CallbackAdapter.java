package com.pit.http.okhttp.callback;

import com.pit.http.okhttp.datahandler.DataHandler;
import okhttp3.Call;
import okhttp3.Response;

import java.util.Map;

/**
 * {@linkplain Callback}的空实现
 *
 * @Author gy
 * @Date 2020-03-20 17:58
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
