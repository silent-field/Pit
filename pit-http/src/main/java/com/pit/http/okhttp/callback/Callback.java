package com.pit.http.okhttp.callback;

import com.pit.http.okhttp.datahandler.DataHandler;
import okhttp3.Call;
import okhttp3.Response;

import java.util.Map;

/**
 * @author gy
 * 异步请求的回调接口
 */
public interface Callback<T> {
    /**
     * 在请求前调用
     *
     * @param url
     * @param body
     * @param headerMap
     */
    void onBefore(String url, String body, Map<String, String> headerMap);

    /**
     * 请求失败调用
     *
     * @param call
     * @param exception
     */
    void onError(Call call, Exception exception);

    /**
     * 请求完成调用
     *
     * @param response
     */
    void onComplete(Response response);

    /**
     * 数据处理器,用于解析转换响应结果
     *
     * @return
     */
    DataHandler<T> getDataHandler();

    /**
     * 根据数据处理器得到处理结果,调用者直接使用处理后的数据
     *
     * @param data
     */
    void onSuccess(T data);
}
