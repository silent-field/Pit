package com.andrew.common.mq.kafka.consumer;

public interface IKafkaMsgProcessor {
	/**
	 * 处理消息接口
	 * @param msg
	 * @throws Exception
	 */
	void handleMessage(MessageEvent msg) throws Exception;

	/**
	 * 和配置文件里的consumerId绑定
	 * @return
	 */
	String bindConsumerId();
}