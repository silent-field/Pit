package com.andrew.common.text.json;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

/**
 * JacksonUtils
 *
 * @Author Andrew
 * @Date 2019-06-13 12:00
 */
public class JacksonUtils {
	private JacksonUtils() {

	}

	/**
	 * 判断是否为合法的JSON字符串
	 *
	 * @param input
	 * @return
	 */
	public static boolean isValidJSONString(String input) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.readTree(input);
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	/**
	 * 判断字符串是否合法，并返回一个JsonNode
	 *
	 * @param input
	 * @return
	 */
	public static Pair<Boolean, JsonNode> checkAndGetJsonNode(String input) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(input);
			return ImmutablePair.of(true, jsonNode);
		} catch (IOException ex) {
			return ImmutablePair.of(false, null);
		}
	}


	public static <T> Pair<Boolean, T> checkAndGetObject(String input, Class<T> valueType) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			T result = objectMapper.readValue(input, valueType);
			return ImmutablePair.of(true, result);
		} catch (IOException ex) {
			return ImmutablePair.of(false, null);
		}
	}
}
