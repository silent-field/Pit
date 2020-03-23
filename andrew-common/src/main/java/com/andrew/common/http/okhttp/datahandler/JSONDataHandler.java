package com.andrew.common.http.okhttp.datahandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Response;

import java.io.IOException;

/**
 * @Description
 *
 * @Author Andrew
 * @Date 2019-06-20 16:36
 */
public class JSONDataHandler implements DataHandler<JSONObject>{
	@Override
	public JSONObject handle(Response response) throws IOException {
		return JSON.parseObject(response.body().string());
	}
}