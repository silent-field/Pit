package com.andrew.common.text.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;

/**
 * FastJsonUtils
 *
 * @Author Andrew
 * @Date 2019-06-13 12:00
 */
public class FastJsonUtils {
	private FastJsonUtils() {

	}

	/**
	 * 判断是否为合法的JSONObject字符串
	 *
	 * @param input
	 * @return
	 */
	public static boolean isValidJSONObject(String input) {
		try {
			JSONObject.parseObject(input);
		} catch (JSONException ex) {
			return false;
		}
		return true;
	}

	/**
	 * 判断是否为合法的JSONArray字符串
	 *
	 * @param input
	 * @return
	 */
	public static boolean isValidJSONArray(String input) {
		try {
			JSONObject.parseArray(input);
		} catch (JSONException ex) {
			return false;
		}
		return true;
	}

	/**
	 * 判断字符串是否合法，并返回一个JSONObject
	 *
	 * @param input
	 * @return
	 */
	public static Pair<Boolean, JSONObject> checkAndGetJSONObject(String input) {
		try {
			JSONObject result = JSONObject.parseObject(input);
			return ImmutablePair.of(true, result);
		} catch (JSONException ex) {
			return ImmutablePair.of(false, null);
		}
	}


	public static <T> Pair<Boolean, T> checkAndGetObject(String input, Type clazz) {
		try {
			T result = JSON.parseObject(input, clazz);
			return ImmutablePair.of(true, result);
		} catch (JSONException ex) {
			return ImmutablePair.of(false, null);
		}
	}
}
