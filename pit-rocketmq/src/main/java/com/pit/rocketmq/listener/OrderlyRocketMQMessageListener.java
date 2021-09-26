package com.pit.rocketmq.listener;

import com.pit.rocketmq.consumer.DefaultRocketMQConsumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Slf4j
public class OrderlyRocketMQMessageListener implements MessageListenerOrderly {
    @Setter
    private List<IRocketMQMsgHandler> handlers;

    private DefaultRocketMQConsumer consumer;

    public OrderlyRocketMQMessageListener(DefaultRocketMQConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
        MessageExt newest = null;
        for (MessageExt messageExt : list) {
            newest = messageExt;
            // 可以用多个handler去做同一个messageExt的业务处理
            for (IRocketMQMsgHandler handler : handlers) {
                //处理失败会把这个消息放队列里面等待再次处理
                try {
                    handler.handleMessage(messageExt);
                } catch (Exception e) {
                    log.error("RocketMQ consume exception.", e);
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }
        }
        consumer.setNewest(newest);
        return ConsumeOrderlyStatus.SUCCESS;
    }
}
