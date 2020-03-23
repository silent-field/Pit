package com.andrew.common.mq.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

@Slf4j
public class KafkaEmptyProducerCallback implements Callback {
	private String topic;

	private byte[] msg;

	private KafkaMessageProducer kafkaMessageProducer;

	private int retryTimes;

	public KafkaEmptyProducerCallback(String topic, byte[] msg, KafkaMessageProducer kafkaMessageProducer, int retryTimes) {
		this.topic = topic;
		this.msg = msg;
		this.kafkaMessageProducer = kafkaMessageProducer;
		this.retryTimes = retryTimes;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.apache.kafka.clients.producer.Callback#onCompletion(org.apache.kafka.
	 * clients.producer.RecordMetadata, java.lang.Exception)
	 */
	@Override
	public void onCompletion(RecordMetadata metadata, Exception exception) {
		if (null != exception) {
			log.error(topic + " KafkaEmptyCallback fail:", exception);
			if (retryTimes > 0) {
				kafkaMessageProducer.send(topic, msg, retryTimes - 1);
			}
		}
	}

}
