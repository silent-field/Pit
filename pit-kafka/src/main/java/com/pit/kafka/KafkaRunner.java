package com.pit.kafka;

import com.pit.kafka.factory.KafkaFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/27
 */
public class KafkaRunner implements ApplicationListener<ApplicationReadyEvent> {
    private KafkaFactory kafkaFactory;

    public KafkaRunner(KafkaFactory kafkaFactory) {
        this.kafkaFactory = kafkaFactory;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        kafkaFactory.startAllConsumer();
        kafkaFactory.startAllProducer();
    }
}
