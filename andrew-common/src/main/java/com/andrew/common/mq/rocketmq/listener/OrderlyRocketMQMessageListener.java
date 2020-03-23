package com.andrew.common.mq.rocketmq.listener;

import com.andrew.common.mq.rocketmq.checker.RocketMQConsumeChecker;
import com.andrew.common.mq.rocketmq.config.RocketMQConsumerConfiguration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

@Slf4j
public class OrderlyRocketMQMessageListener implements MessageListenerOrderly {

	/**
	 * 一个listener绑多个process
	 */
	@Setter
	private List<IRocketMQMsgProcessor> processorList;

	private RocketMQConsumeChecker consumeChecker = null;

	public OrderlyRocketMQMessageListener(RocketMQConsumerConfiguration config) {
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
	public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
		for (MessageExt messageExt : list) {
			//可以适配一个监听器有多个processor的情况，可以用多个processor去做同一个messageExt的业务处理
			for (IRocketMQMsgProcessor processor : processorList) {
				//处理失败会把这个消息放队列里面等待再次处理
				try {
					if (consumeChecker != null) {
						consumeChecker.checkBacklogQuietly(messageExt);
					}

					processor.handleMessage(messageExt);
				} catch (Exception e) {
					log.error("OrderlyRocketMQMessageListener error", e);
					if (consumeChecker != null) {
						consumeChecker.failAlarmQuietly(e.getMessage());
					}
					return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
				}
			}
		}
		return ConsumeOrderlyStatus.SUCCESS;
	}
}
