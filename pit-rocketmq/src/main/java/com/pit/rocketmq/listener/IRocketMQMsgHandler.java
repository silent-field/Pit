package com.pit.rocketmq.listener;

import org.apache.rocketmq.common.message.MessageExt;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
public interface IRocketMQMsgHandler {
    /**
     * 处理消息接口
     * @param msg
     * @throws Exception
     */
    void handleMessage(MessageExt msg) throws Exception;

    /**
     * 和配置文件里的consumer topic绑定
     * @return
     */
    String topic();
}
