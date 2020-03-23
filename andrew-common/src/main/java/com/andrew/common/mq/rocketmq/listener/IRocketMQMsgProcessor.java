package com.andrew.common.mq.rocketmq.listener;

import org.apache.rocketmq.common.message.MessageExt;

public interface IRocketMQMsgProcessor {
	/**
	 * 处理消息接口
	 * @param msg
	 * @throws Exception
	 */
	void handleMessage(MessageExt msg) throws Exception;

	/**
	 * 和配置文件里的consumerId绑定
	 * @return
	 */
	String bindConsumerId();
}
