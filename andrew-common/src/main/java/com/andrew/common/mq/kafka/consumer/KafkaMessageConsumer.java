package com.andrew.common.mq.kafka.consumer;

import com.andrew.common.log.LogUtil;
import com.andrew.common.mq.kafka.config.KafkaConsumerConfiguration;
import com.andrew.common.text.ValueUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Andre一个consumer对应一个线程
 *
 * @Author Andrew
 * @Date 2020-03-20 11:47
 */
@Slf4j
public class KafkaMessageConsumer implements Runnable {
	/**
	 * 消费者ID
	 */
	private String consumerId;

	private String consumerDesc;

	private volatile boolean running = false;
	private Object shutdownLck = new Object();

	private KafkaConsumer<String, String> consumer;

	private KafkaConsumerConfiguration config;
	private List<IKafkaMsgProcessor> processors;

	public KafkaMessageConsumer(KafkaConsumerConfiguration config, List<IKafkaMsgProcessor> processors) {
		checkConfig(config);
		init(config);
		this.config = config;
		this.processors = processors;
	}

	public void init(KafkaConsumerConfiguration config) {
		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getServers());
		props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());

		consumerDesc = config.getGroupId() + "@" + config.getTopic();

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, ValueUtil.getOrDefault(config.getAutoCommitIntervalMs(), 1000));
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, ValueUtil.getOrDefault(config.getAutoOffsetReset(), "earliest"));
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, ValueUtil.getOrDefault(config.getMaxPollRecords(), 100));

		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(config.getTopic()));
	}

	public synchronized void start() {
		if (running) {
			return;
		}

		Thread t = new Thread(this);
		t.setName(consumer.getClass().getSimpleName() + "-main");
		t.start();

		log.info("KafkaMessageConsumer started consumer:{}", consumerDesc);
	}

	private void checkConfig(KafkaConsumerConfiguration config) {
		if (StringUtils.isAnyBlank(config.getServers(), config.getGroupId(), config.getTopic(), config.getProcessor())) {
			throw new IllegalStateException("rocketmq.config.servers/groupid/topic/processor不能为空");
		}
	}

	public synchronized void shutdown() {
		synchronized (shutdownLck) {
			running = false;
			try {
				shutdownLck.wait(3000L);
			} catch (InterruptedException e) {
				log.error("KafkaMessageConsumer stop", e);
				Thread.currentThread().interrupt();
			}
		}
		log.info("KafkaMessageConsumer stopped consumer:{}", consumerDesc);
	}

	@Override
	public void run() {
		try {
			while (running) {
				try {
					// 1000ms内等待Kafka broker返回数据.不管有没有可用的数据都要返回
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));
					for (ConsumerRecord<String, String> record : records) {
						LogUtil.debug(log, "{}->Message: topic[{}],partition[{}],offset[{}],key[{}],value[{}]",
								consumerDesc, record.topic(), record.partition(), record.offset(), record.key(),
								record.value());

						MessageEvent messageEvent = new MessageEvent(record.value());
						messageEvent.putHeader(MessageEvent.TIMESTAMP, String.valueOf(System.currentTimeMillis()));
						messageEvent.putHeader(MessageEvent.TOPIC, config.getTopic());
						messageEvent.putHeader(MessageEvent.PARTITION, record.partition());
						messageEvent.putHeader(MessageEvent.OFFSET, record.offset());
						if (record.key() != null) {
							messageEvent.putHeader(MessageEvent.KEY, record.key());
						}

						for (IKafkaMsgProcessor processor : processors) {
							processor.handleMessage(messageEvent);
						}
					}
				} catch (Exception e) {
					log.error(consumerDesc + " Kafka consume EXCEPTION", e);
				}
			}
		} finally {
			consumer.close();
		}

		synchronized (shutdownLck) {
			shutdownLck.notifyAll();
		}
	}
}
