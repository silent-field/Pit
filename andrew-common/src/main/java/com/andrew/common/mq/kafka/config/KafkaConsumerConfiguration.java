package com.andrew.common.mq.kafka.config;

import lombok.Data;

import java.util.Map;

/**
 * kafka 消费者配置项
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Data
public class KafkaConsumerConfiguration {

	/**
	 * kafka消费者组名
	 */
	private String groupId;

	private String topic;

	/**
	 * kafka消费者server地址
	 */
	private String servers;

	private String appName;

	private String processor;

	private Integer autoCommitIntervalMs;

	private String autoOffsetReset;

	private Integer maxPollRecords;

	private String consumerId;
}
