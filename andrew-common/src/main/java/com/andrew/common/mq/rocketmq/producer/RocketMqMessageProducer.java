package com.andrew.common.mq.rocketmq.producer;

import com.andrew.common.mq.rocketmq.config.RocketMQProducerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;

@Slf4j
public class RocketMqMessageProducer {

	/**
	 * 生产者ID
	 */
	private String producerId;

	private DefaultMQProducer defaultMQProducer;

	public RocketMqMessageProducer(RocketMQProducerConfiguration config) {
		if (StringUtils.isBlank(config.getProducerId())) {
			log.error("rocketMqProducer's getProducerId is null, init failed");
			return;
		}

		if (StringUtils.isBlank(config.getGroupId())) {
			log.error("rocketMqProducer's getGroupId is null, init failed");
			return;
		}

		if (StringUtils.isBlank(config.getNamesrvAddr())) {
			log.error("rocketMqProducer's getNamesrvAddr is null, init failed");
			return;
		}

		this.producerId = config.getProducerId();
		this.defaultMQProducer = new DefaultMQProducer(config.getGroupId());
		this.defaultMQProducer.setNamesrvAddr(config.getNamesrvAddr());

		if (config.getSendMsgTimeout() != null) {
			this.defaultMQProducer.setSendMsgTimeout(config.getSendMsgTimeout());
		}

		if (config.getMaxMessageSize() != null) {
			this.defaultMQProducer.setMaxMessageSize(config.getMaxMessageSize());
		}
	}

	public void start() throws MQClientException {
		this.defaultMQProducer.start();
	}

	public void shutdown() {
		this.defaultMQProducer.shutdown();
	}

	/**
	 * 同步消费
	 *
	 * @return
	 */
	public SendResult sendMessageSync(Message message, String businessId) throws Exception {
		if (StringUtils.isBlank(message.getTopic())) {
			throw new Exception(producerId + " topic is null");
		}
		try {
			SendResult sendResult = null;
			if (businessId == null) {
				sendResult = this.defaultMQProducer.send(message);
			} else {
				sendResult = this.defaultMQProducer.send(message, new SelectMessageQueueByHash(), businessId);
			}
			return sendResult;
		} catch (Exception e) {
			log.error(producerId + " sendMessageSync error");
			throw e;
		}
	}

	/**
	 * 同步消费而且没有businessId，就是随机hash选择队列的情况send
	 */
	public SendResult sendMessage(Message message) throws Exception {
		return sendMessageSync(message, null);
	}
}
