package com.pit.http.okhttp.datahandler;

import com.google.gson.JsonElement;
import com.pit.core.json.GsonUtils;
import okhttp3.Response;

import java.io.IOException;

/**
 * @Description
 * @Author gy
 * @Date 2019-06-20 16:36
 */
public class JsonDataHandler implements DataHandler<JsonElement> {
    @Override
    public JsonElement handle(Response response) throws IOException {
        return GsonUtils.parseJsonElement(response.body().string());
    }
}