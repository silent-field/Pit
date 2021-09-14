package com.pit.http.okhttp;

import lombok.Data;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * @Description
 *
 * @Author gy
 * @Date 2020-03-20 18:14
 */
public class Builder {
    private Builder() {
    }

    /**
     * 创建OkHttp get request
     * @param url
     * @param headerMap
     * @return
     */
    public static Request newGetReq(String url, Map<String, String> headerMap) {
        return new Request.Builder().url(url).headers(map2Headers(headerMap)).build();
    }

    /**
     * 创建OkHttp post request
     * @param url
     * @param jsonBody
     * @param headerMap
     * @return
     */
    public static OkHttpRequestWrapper newPostReq(String url, String jsonBody, Map<String, String> headerMap) {
        RequestBody body = RequestBody.create(null, jsonBody);
        Request okHttpReqeust = new Request.Builder().url(url).headers(map2Headers(headerMap)).post(body).build();
        return new OkHttpRequestWrapper(okHttpReqeust, url, jsonBody, headerMap);
    }

    /**
     * 强制Content-Type必须是application/json
     * @param headerMap
     * @return
     */
    private static Headers map2Headers(Map<String, String> headerMap) {
        Headers.Builder builder = new Headers.Builder();

        if (MapUtils.isNotEmpty(headerMap)) {
            for (Map.Entry<String, String> header : headerMap.entrySet()) {
                builder.add(header.getKey(), header.getValue());
            }
        }

        return builder.build();
    }

    @Data
    public static class OkHttpRequestWrapper {
        private Request okHttpRequest;
        private String originUrl;
        private String originBody;
        private Map<String, String> headers;
        public OkHttpRequestWrapper(Request okHttpRequest, String originUrl, String originBody,
                                    Map<String, String> headers) {
            this.okHttpRequest = okHttpRequest;
            this.originUrl = originUrl;
            this.originBody = originBody;
            this.headers = headers;
        }
    }
}
