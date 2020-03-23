package com.andrew.common.mq.rocketmq.listener;

import com.andrew.common.mq.rocketmq.checker.RocketMQConsumeChecker;
import com.andrew.common.mq.rocketmq.config.RocketMQConsumerConfiguration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

@Slf4j
public class CurrentlyRocketMQMessageListener implements MessageListenerConcurrently {
	/**
	 * 一个listener绑多个process
	 */
	@Setter
	private List<IRocketMQMsgProcessor> processorList;

	private RocketMQConsumeChecker consumeChecker = null;

	public CurrentlyRocketMQMessageListener(RocketMQConsumerConfiguration config) {
		if (config.isAlarm()) {
			int backlogThreshold = config.getBacklogThreshold() <= 0 ?
					RocketMQConsumeChecker.DEFAULT_THRESHOLD :
					config.getBacklogThreshold();
			int interval = config.getAlarmCheckInterval() <= 0 ?
					RocketMQConsumeChecker.DEFAULT_INTERVAL :
					config.getAlarmCheckInterval();

			checkConfig(config);
			consumeChecker = new RocketMQConsumeChecker(config.getConsumerId(), backlogThreshold, interval);
		}
	}

	private void checkConfig(RocketMQConsumerConfiguration config) {
		if (StringUtils.isBlank(config.getProcessor())) {
			throw new IllegalStateException("rocketmq.config.processor不能为空");
		}
	}

	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list,
													ConsumeConcurrentlyContext consumeConcurrentlyContext) {
		for (MessageExt messageExt : list) {
			for (IRocketMQMsgProcessor processor : processorList) {
				try {
					if (consumeChecker != null) {
						consumeChecker.checkBacklogQuietly(messageExt);
					}

					processor.handleMessage(messageExt);
				} catch (Exception e) {
					//失败的时候会采用一定的退避策略重新拉取
					log.error("ConcurrentlyRocketMQMessageListener error", e);
					if (consumeChecker != null) {
						consumeChecker.failAlarmQuietly(e.getMessage());
					}
					return ConsumeConcurrentlyStatus.RECONSUME_LATER;
				}
			}
		}
		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}
}
