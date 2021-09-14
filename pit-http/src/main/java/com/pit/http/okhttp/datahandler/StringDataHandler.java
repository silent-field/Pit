package com.pit.http.okhttp.datahandler;

import okhttp3.Response;

import java.io.IOException;

/**
 * {@linkplain Response#body()}转成String
 *
 * @Author gy
 * @Date 2019-06-17 15:50
 */
public class StringDataHandler implements DataHandler<String> {
    @Override
    public String handle(Response response) throws IOException {
        return response.body().string();
    }
}
