package com.pit.rocketmq;

import com.pit.rocketmq.factory.RocketMQFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
public class RocketMQStopListener implements ApplicationListener<ContextClosedEvent> {

    private RocketMQFactory rocketMQFactory;

    public RocketMQStopListener(RocketMQFactory rocketMQFactory) {
        this.rocketMQFactory = rocketMQFactory;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        rocketMQFactory.stopAllConsumer();
        rocketMQFactory.stopAllProducer();
    }
}
