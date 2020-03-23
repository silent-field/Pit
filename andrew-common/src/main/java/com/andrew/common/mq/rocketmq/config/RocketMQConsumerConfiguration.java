package com.andrew.common.mq.rocketmq.config;

import lombok.Data;

import java.util.Map;

/**
 * RocketMQ 消费者配置项
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Data
public class RocketMQConsumerConfiguration {

	/**
	 * rocketMq消费者组名
	 */
	private String groupId;

	/**
	 * rocketMq消费者server地址
	 */
	private String namesrvAddr;

	/**
	 * 消费者Id
	 */
	private String consumerId;

	/**
	 * 消费者订阅的多个主题和tag，一个主题下有多个tag可以用 || 分割，例如：TagA || TagC || TagD
	 */
	private Map<String, String> topicAndTagMap;

	/**
	 * 消费者是否要进行有序消费
	 */
	private boolean orderly;

	/**
	 * 根据消费者Id去指定各种参数
	 */
	private Map<String, String> consumerOptions;

	/**
	 * 消费者告警
	 */
	private boolean alarm;

    /**
     * 消费积压阈值
     */
    private int backlogThreshold;

	/**
	 * 告警检查间隔
	 */
	private int alarmCheckInterval;

	private String processor;

	private String appName;

	@Override
	public String toString() {
		return "MQConsumerConfiguration{" + "groupId='" + groupId + '\'' + ", namesrvAddr='" + namesrvAddr + '\''
				+ ", consumerId='" + consumerId + '\'' + ", topicAndTagMap=" + topicAndTagMap + ", orderly=" + orderly
				+ ", consumerOptions=" + consumerOptions + ", alarm=" + alarm + ", alarmCheckInterval="
				+ alarmCheckInterval + ", backlogThreshold=" + backlogThreshold + '}';
	}
}
