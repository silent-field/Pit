package com.andrew.common.mq.rocketmq.config;

import lombok.Data;

import java.util.Map;

/**
 * RocketMQ 生产者配置项
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Data
public class RocketMQProducerConfiguration {
	/**
	 * 生产者ID，用于生产者唯一标识
	 */
	private String producerId;
	/**
	 * rocketMQ生产者群组名字，可以把一类的生产者归为一组
	 */
	private String groupId;

	/**
	 * rocketMQ的nameServer地址，服务地址
	 */
	private String namesrvAddr;

	/**
	 * 发送消息超时时间，单位毫秒
	 */
	private Integer sendMsgTimeout;
	/**
	 * 限制的消息大小，默认131072（128kb）
	 */
	private Integer maxMessageSize;
	
	/**
	 * 生产者其他参数
	 */
	private Map<String, String> options;

	@Override
	public String toString() {
		return "MQProducerConfiguration{" + "producerId='" + producerId + '\'' + ", groupId='" + groupId + '\''
				+ ", namesrvAddr='" + namesrvAddr + '\'' + ", sendMsgTimeout=" + sendMsgTimeout + ", maxMessageSize="
				+ maxMessageSize + ", options=" + options + '}';
	}
}
