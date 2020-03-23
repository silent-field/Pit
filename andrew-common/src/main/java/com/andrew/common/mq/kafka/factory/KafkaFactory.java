package com.andrew.common.mq.kafka.factory;

import com.andrew.common.mq.kafka.config.KafkaConsumerConfiguration;
import com.andrew.common.mq.kafka.config.KafkaProducerConfiguration;
import com.andrew.common.mq.kafka.consumer.IKafkaMsgProcessor;
import com.andrew.common.mq.kafka.consumer.KafkaMessageConsumer;
import com.andrew.common.mq.kafka.producer.KafkaMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来集中管理所有的consumer线程池，包括创建和销毁
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Slf4j
public class KafkaFactory {

	/**
	 * 用于存放已经存在的消费者map组,用concurrentHashMap保证并发性
	 */
	private static Map<String, KafkaMessageConsumer> consumers = new ConcurrentHashMap<>();

	private static Map<String, KafkaMessageProducer> producers = new ConcurrentHashMap<>();

    /**
     * 不允许直接new
     */
	private KafkaFactory(){
    }

	private static class SingletonHolder {
		static final KafkaFactory instance = new KafkaFactory();
	}

	public static KafkaFactory getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * 创建一个生产者
	 */
	public KafkaMessageProducer createProducer(KafkaProducerConfiguration config) {
		if (producers.containsKey(config.getProducerId())) {
			return producers.get(config.getProducerId());
		}

		KafkaMessageProducer producer = new KafkaMessageProducer(config);
		producers.put(config.getProducerId(), producer);
		log.info("MQProducer start success, detail :{}" + config.toString());

		return producer;
	}

	/**
	 * 创建一个消费者
	 */
	public KafkaMessageConsumer createConsumer(KafkaConsumerConfiguration config, List<IKafkaMsgProcessor> list) {
		//如果Map里面存在就直接放回，不重复创建消费者
		if (consumers.containsKey(config.getConsumerId())) {
			return consumers.get(config.getConsumerId());
		}

		try {
			//根据消费者配置文件初始化消费者
			KafkaMessageConsumer consumer = new KafkaMessageConsumer(config, list);
			consumer.start();
			consumers.put(config.getConsumerId(), consumer);
			log.info("rocketMQConsumer start success, detail :" + config.toString());
			log.info("rocketMQConsumer processors list size :" + list.size());
			return consumer;
		} catch (Exception e) {
			log.error("rocketMQConsumer start error" + config.toString(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取某个消费者
	 */
	public KafkaMessageConsumer getConsumer(String consumerId) {
		if (StringUtils.isNotBlank(consumerId) && consumers.containsKey(consumerId)) {
			return consumers.get(consumerId);
		}
		return null;
	}

	/**
	 * 获取某个生产者
	 */
	public KafkaMessageProducer getProducer(String producerId) {
		if (StringUtils.isNotBlank(producerId) && producers.containsKey(producerId)) {
			return producers.get(producerId);
		}
		return null;
	}

	/**
	 * 停止某个消费者
	 */
	public void stopConsumer(String consumerId) {
		if (StringUtils.isNotBlank(consumerId) && consumers.containsKey(consumerId)) {
			consumers.get(consumerId).shutdown();
			consumers.remove(consumerId);
			log.info("KafkaMessageConsumer :" + consumerId + "is shutdown");
		}
	}

	/**
	 * 停止某个生产者
	 */
	public void stopProducer(String producerId) {
		if (StringUtils.isNotBlank(producerId) && producers.containsKey(producerId)) {
			producers.get(producerId).shutdown();
			producers.remove(producerId);
			log.info("KafkaMessageProducer :" + producerId + "is shutdown");
		}
	}


	/**
	 * 停止所有消费者
	 */
	public void stopAllConsumer() {
		for (Map.Entry<String, KafkaMessageConsumer> entry : consumers.entrySet()) {
			entry.getValue().shutdown();
			log.info("KafkaMessageConsumer :" + entry.getKey() + "is shutdown");
		}
		log.info("All KafkaMessageConsumer is shutdown");
	}

	/**
	 * 停止所有生产者
	 */
	public void stopAllProducer() {
		for (Map.Entry<String, KafkaMessageProducer> entry : producers.entrySet()) {
			entry.getValue().shutdown();
			log.info("KafkaMessageProducer :" + entry.getKey() + "is shutdown");
		}
		log.info("All KafkaMessageProducer is shutdown");
	}

}
