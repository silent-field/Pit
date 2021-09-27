package com.pit.kafka;

import com.pit.kafka.factory.KafkaFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/27
 */
public class KafkaStopListener implements ApplicationListener<ContextClosedEvent> {

    private KafkaFactory kafkaFactory;

    public KafkaStopListener(KafkaFactory kafkaFactory) {
        this.kafkaFactory = kafkaFactory;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        kafkaFactory.stopAllConsumer();
        kafkaFactory.stopAllProducer();
    }
}
