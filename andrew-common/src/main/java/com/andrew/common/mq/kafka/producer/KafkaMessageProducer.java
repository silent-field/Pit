package com.andrew.common.mq.kafka.producer;

import com.andrew.common.mq.kafka.config.KafkaProducerConfiguration;
import com.andrew.common.text.ValueUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @Author Andrew
 * @Date 2020-3-20
 */
@Slf4j
public class KafkaMessageProducer {

	private volatile Producer<String, byte[]> producer = null;

	public void send(String topic, byte[] msg, int retryTimes) {
		if (null == producer) {
			log.error("producer is not init");
			return;
		}
		KafkaEmptyProducerCallback callback = new KafkaEmptyProducerCallback(topic, msg, this, retryTimes);
		producer.send(new ProducerRecord<String, byte[]>(topic, msg), callback);
	}

	public KafkaMessageProducer(KafkaProducerConfiguration config) {
		init(config);
	}

	private synchronized void init(KafkaProducerConfiguration config) {
		if (null == config) {
			return;
		}
		Properties properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getServers());
		// all所有follower都响应了才认为消息提交成功，即"committed",1表示主确认就行
		properties.put(ProducerConfig.ACKS_CONFIG, ValueUtil.getOrDefault(config.getAcks(), "1"));
		// retries=MAX:无限重试
		properties.put(ProducerConfig.RETRIES_CONFIG, ValueUtil.getOrDefault(config.getRetries(), "3"));
		// producer将试图批处理消息记录，以减少请求次数.默认的批量处理消息字节数,batch.size当批量的数据大小达到设定值后，就会立即发送，不顾下面的linger.ms
		properties.put(ProducerConfig.BATCH_SIZE_CONFIG, ValueUtil.getOrDefault(config.getBatchSize(), 2048));
		// 延迟10ms发送，这项设置将通过增加小的延迟来完成--即，不是立即发送一条记录，producer将会等待给定的延迟时间以允许其他消息记录发送，这些消息记录可以批量处理
		properties.put(ProducerConfig.LINGER_MS_CONFIG, ValueUtil.getOrDefault(config.getLingerMs(), 10));
		// 缓存数据的内存大小
		properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, ValueUtil.getOrDefault(config.getBufferMemory(), 67108864));
		// timeout
		properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, ValueUtil.getOrDefault(config.getMaxBlockMs(), 6000));
		properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, KafkaProducerPartitioner.class.getName());// 分片逻辑
		properties = ProducerConfig.addSerializerToConfig(properties,
				new org.apache.kafka.common.serialization.StringSerializer(),
				new org.apache.kafka.common.serialization.ByteArraySerializer());
		this.producer = new org.apache.kafka.clients.producer.KafkaProducer<String, byte[]>(properties);
	}

	public void shutdown() {
		if (null != producer) {
			producer.close();
		}
	}
}
