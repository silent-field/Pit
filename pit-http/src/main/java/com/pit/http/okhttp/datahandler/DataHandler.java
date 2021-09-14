package com.pit.http.okhttp.datahandler;

import java.io.IOException;

/**
 * @Description
 * @Author gy
 * @Date 2019-06-20 16:36
 */
public interface DataHandler<T> {
    /**
     * 应用服务器返回{@linkplain okhttp3.Response}后,将{@linkplain okhttp3.Response}转为需要的数据格式
     * @param response
     * @return
     * @throws IOException
     */
    T handle(final okhttp3.Response response) throws IOException;
}