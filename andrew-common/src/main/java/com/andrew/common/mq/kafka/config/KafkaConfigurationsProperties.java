package com.andrew.common.mq.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RocketMQ 消费者、生产者配置项
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Data
@ConfigurationProperties(prefix = "kafka.config")
@Component
public class KafkaConfigurationsProperties {
	private List<KafkaConsumerConfiguration> consumers;

	private List<KafkaProducerConfiguration> producers;
}
