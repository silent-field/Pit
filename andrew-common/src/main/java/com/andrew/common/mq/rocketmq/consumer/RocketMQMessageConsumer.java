package com.andrew.common.mq.rocketmq.consumer;

import com.andrew.common.mq.rocketmq.config.RocketMQConsumerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 初始化rocketMq消费者，一个消费者类可以看做一个线程池
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Slf4j
public class RocketMQMessageConsumer extends DefaultMQPushConsumer {
	/** 消费者ID */
	private String consumerId;

	/**
	 * 初始化配置文件里面的各种消费者属性配置
	 * @param
	 */
	public RocketMQMessageConsumer(RocketMQConsumerConfiguration config){
		if(StringUtils.isBlank(config.getConsumerId())){
			log.error("rocketMQConsumer's getConsumerId is null, init failed");
			return;
		}

		if(StringUtils.isBlank(config.getGroupId())){
			log.error("rocketMQConsumer's getGroupId is null, init failed");
			return;
		}

		if(StringUtils.isBlank(config.getNamesrvAddr())){
			log.error("rocketMQConsumer's getNamesrvAddr is null, init failed");
			return;
		}

		if(CollectionUtils.isEmpty(config.getTopicAndTagMap())){
			log.error("rocketMQConsumer's getTopicAndTagMap is null, init failed");
			return;
		}

		if(CollectionUtils.isEmpty(config.getConsumerOptions())){
			log.warn("rocketMQConsumer's getConsumerOptions is null, init failed");
		}

		this.consumerId = config.getConsumerId();
		this.subscribe(config.getTopicAndTagMap());
		super.setConsumerGroup(config.getGroupId());
		super.setNamesrvAddr(config.getNamesrvAddr());

		//设置消费者其它参数
		if(!CollectionUtils.isEmpty(config.getConsumerOptions())){
			String consumeFromWhere = config.getConsumerOptions().get("consumeFromWhere");
			String consumeThreadMin = config.getConsumerOptions().get("consumeThreadMin");
			String consumeThreadMax = config.getConsumerOptions().get("consumeThreadMax");
			String pullThresholdForQueue = config.getConsumerOptions().get("pullThresholdForQueue");
			String consumeMessageBatchMaxSize = config.getConsumerOptions().get("consumeMessageBatchMaxSize");
			String pullBatchSize = config.getConsumerOptions().get("pullBatchSize");
			String pullInterval = config.getConsumerOptions().get("pullInterval");
			if (StringUtils.isNotBlank(consumeFromWhere)) {
				if (StringUtils.equals(consumeFromWhere, "CONSUME_FROM_LAST_OFFSET")) {
					super.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
				} else if (StringUtils.equals(consumeFromWhere, "CONSUME_FROM_FIRST_OFFSET")) {
					super.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
				}
			}
			if (StringUtils.isNotBlank(consumeThreadMin)) {
				super.setConsumeThreadMin(Integer.parseInt(consumeThreadMin));
			}
			if (StringUtils.isNotBlank(consumeThreadMax)) {
				super.setConsumeThreadMax(Integer.parseInt(consumeThreadMax));
			}
			if (StringUtils.isNotBlank(pullThresholdForQueue)) {
				super.setPullThresholdForQueue(Integer.parseInt(pullThresholdForQueue));
			}
			if (StringUtils.isNotBlank(consumeMessageBatchMaxSize)) {
				super.setConsumeMessageBatchMaxSize(Integer.parseInt(consumeMessageBatchMaxSize));
			}
			if (StringUtils.isNotBlank(pullBatchSize)) {
				super.setPullBatchSize(Integer.parseInt(pullBatchSize));
			}
			if (StringUtils.isNotBlank(pullInterval)) {
				super.setPullInterval(Integer.parseInt(pullInterval));
			}
		}

	}

	/**
	 * 订阅指定主题，可能有多个topic而且多个tag
	 */
	void subscribe(Map<String,String> topicAndTagMap){

		for(Map.Entry<String, String> entry : topicAndTagMap.entrySet()){
			String topic = entry.getKey();
			String tag = entry.getValue();

			try {
				this.subscribe(topic, tag);
			}catch (MQClientException e){
				log.error("rocketMQConsumer's topic or tag is null , init failed");
			}
		}
	}

}
