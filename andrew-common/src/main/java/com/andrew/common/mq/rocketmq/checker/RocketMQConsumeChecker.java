package com.andrew.common.mq.rocketmq.checker;

import com.andrew.common.alarm.Alarm;
import com.andrew.common.limit.SimpleCountLimit;
import com.andrew.common.limit.SimpleCountLimitGrantWhitTime;
import com.andrew.common.text.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.concurrent.TimeUnit;

/**
 * AndreRocketMQ消费积压监控
 * @Author Andrew
 * @Date 2019-09-04 16:20
 */
@Slf4j
public class RocketMQConsumeChecker {
	// 默认1分钟采集一次数据
	private SimpleCountLimitGrantWhitTime countLimit;

	public static final int DEFAULT_THRESHOLD = 1000;

	public static final int DEFAULT_INTERVAL = 1000;

	private String consumerId;

	// 默认积压达到1000就告警
	private int backlogThreshold = DEFAULT_THRESHOLD;

	// 默认1秒检查一次
	private int interval = DEFAULT_INTERVAL;

	public RocketMQConsumeChecker(String consumerId, Integer backlogThreshold, Integer interval) {
		this.consumerId = consumerId;
		if (backlogThreshold != null) {
			this.backlogThreshold = backlogThreshold;
		}

		if (interval != null) {
			this.interval = interval;
		}

		countLimit = new SimpleCountLimitGrantWhitTime(1, this.interval, TimeUnit.MILLISECONDS);
	}

	/**
	 * 检查积压告警，try-catch所有异常
	 *
	 * @param msg
	 */
	public void checkBacklogQuietly(MessageExt msg) {
		try {
			/**
			 * 监控告警
			 */
			long curTime = System.currentTimeMillis();
			boolean grant = countLimit.grant(curTime);
			if (grant) {
				long queueOffset = msg.getQueueOffset();
				long maxOffset = Long.parseLong(msg.getProperties().get(MessageConst.PROPERTY_MAX_OFFSET));
				long backlog = maxOffset - queueOffset;
				// 毫秒值
				Long storeTimestamp = msg.getStoreTimestamp();

				log.info(StringUtil
						.format("消费信息采样：consumerId[{}],消息[{}],消息生产时间:[{}],queueId[{}],queueOffset[{}],maxOffset是[{}],当前消费组的积压量:[{}]",
								consumerId, new String(msg.getBody()), storeTimestamp, msg.getQueueId(), queueOffset,
								maxOffset, backlog));

				// 检查积压数告警
				if (backlog >= backlogThreshold) {
					String errorMsg = StringUtil
							.format("消费信息采样发现消费积压：consumerId[{}],消息生产时间:{},queueId[{}],queueOffset[{}],maxOffset是[{}],当前消费组的积压量:[{}]",
									consumerId, storeTimestamp, msg.getQueueId(), queueOffset, maxOffset, backlog);
					log.error(errorMsg);
				}
			}
		} catch (Exception e) {
			log.error(StringUtil.format("消费积压检查发生异常,consumerId:[{}]", consumerId), e);
		}
	}

	/**
	 * 异常告警，try-catch所有异常
	 *
	 * @param errorMsg
	 */
	public void failAlarmQuietly(String errorMsg) {
		try {
			log.error(errorMsg);
		} catch (Exception e) {
			log.error("RocketMQConsumeChecker.fail 发送告警失败", e);
		}
	}
}
