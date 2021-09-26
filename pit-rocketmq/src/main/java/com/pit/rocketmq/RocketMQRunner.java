package com.pit.rocketmq;

import com.pit.rocketmq.factory.RocketMQFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
public class RocketMQRunner implements ApplicationListener<ApplicationReadyEvent> {
    private RocketMQFactory rocketMQFactory;

    public RocketMQRunner(RocketMQFactory rocketMQFactory) {
        this.rocketMQFactory = rocketMQFactory;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        rocketMQFactory.startAllConsumer();
        rocketMQFactory.startAllProducer();
    }
}
