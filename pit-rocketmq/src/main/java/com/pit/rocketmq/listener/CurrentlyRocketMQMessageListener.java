package com.pit.rocketmq.listener;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Slf4j
public class CurrentlyRocketMQMessageListener implements MessageListenerConcurrently {
    /**
     * 一个listener绑多个process
     */
    @Setter
    private List<IRocketMQMsgHandler> handlers;

    @Getter
    private MessageExt newest;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        for (MessageExt messageExt : list) {
            newest = messageExt;

            for (IRocketMQMsgHandler handler : handlers) {
                try {
                    handler.handleMessage(messageExt);
                } catch (Exception e) {
                    //失败的时候会采用一定的退避策略重新拉取
                    log.error("RocketMQ consume exception.", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
