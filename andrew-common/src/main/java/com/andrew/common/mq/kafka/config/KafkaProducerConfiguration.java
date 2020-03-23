package com.andrew.common.mq.kafka.config;

import lombok.Data;

/**
 * kafka 生产者配置项
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Data
public class KafkaProducerConfiguration {
	/**
	 * kafka servers地址
	 */
	private String servers;

	private String acks;

	private String retries;

	private Integer batchSize;

	private Integer lingerMs;

	private Integer bufferMemory;

	private Integer maxBlockMs;

	/**
	 * 生产者ID，用于生产者唯一标识
	 */
	private String producerId;
}
