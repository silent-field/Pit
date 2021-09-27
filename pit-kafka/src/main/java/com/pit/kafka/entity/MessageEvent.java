package com.pit.kafka.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka消息包装
 */
public class MessageEvent {
	public static final String TIMESTAMP = "timestamp";
	public static final String TOPIC = "topic";
	public static final String OFFSET = "offset";
	public static final String PARTITION = "partition";
	public static final String KEY = "key";

	private Map<String, Object> headers = new HashMap<>();
	private String content;

	public MessageEvent(String content) {
		this.content = content;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void putHeader(String key, Object value) {
		this.headers.put(key, value);
	}

	public String getContent() {
		return content;
	}

}