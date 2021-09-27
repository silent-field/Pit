package com.pit.kafka.factory;

import com.pit.kafka.config.ConsumerConfig;
import com.pit.kafka.config.ProducerConfig;
import com.pit.kafka.consumer.DefaultKafkaConsumer;
import com.pit.kafka.consumer.MessageConsumer;
import com.pit.kafka.producer.DefaultKafkaProducer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/27
 */
@Slf4j
public class KafkaFactory {
    @Getter
    private volatile Map<String, DefaultKafkaConsumer> consumers = new HashMap<>();

    @Getter
    private volatile Map<String, DefaultKafkaProducer> producers = new HashMap<>();

    /**
     * 获取某个消费者
     */
    public DefaultKafkaConsumer getConsumer(String consumerId) {
        if (StringUtils.isNotBlank(consumerId) && consumers.containsKey(consumerId)) {
            return consumers.get(consumerId);
        }
        return null;
    }

    /**
     * 获取某个生产者
     */
    public DefaultKafkaProducer getProducer(String producerId) {
        if (StringUtils.isNotBlank(producerId) && producers.containsKey(producerId)) {
            return producers.get(producerId);
        }
        return null;
    }

    /**
     * @param config
     * @param messageConsumer
     * @return
     */
    public void createConsumer(String bootstrapServers, String env, ConsumerConfig config, MessageConsumer messageConsumer) {
        //如果Map里面存在就直接放回，不重复创建消费者
        if (consumers.containsKey(config.getConsumerId())) {
            return;
        }
        DefaultKafkaConsumer consumer = new DefaultKafkaConsumer(bootstrapServers, env, config, messageConsumer);
        consumers.put(config.getConsumerId(), consumer);
    }

    /**
     * 启动所有消费者
     */
    public void startAllConsumer() {
        for (Map.Entry<String, DefaultKafkaConsumer> entry : consumers.entrySet()) {
            try {
                entry.getValue().start();
            } catch (Exception e) {
                throw new RuntimeException("start consumer error", e);
            }
            log.info("KafkaConsumer : " + entry.getKey() + " is start");
        }
        log.info("All KafkaConsumer is start");
    }

    /**
     * 停止所有消费者
     */
    public void stopAllConsumer() {
        log.info("stopAllConsumer start");
        for (Map.Entry<String, DefaultKafkaConsumer> entry : consumers.entrySet()) {
            try {
                entry.getValue().stop();
            } catch (Exception e) {
                throw new RuntimeException("stop consumer error", e);
            }
            log.info("KafkaConsumer : " + entry.getKey() + " is stop");
        }
        log.info("All KafkaConsumer is stop");
    }

    /**
     * @param config
     * @return
     */
    public void createProducer(String bootstrapServers, String env, ProducerConfig config) {
        //如果Map里面存在就直接放回，不重复创建消费者
        if (producers.containsKey(config.getProducerId())) {
            return;
        }
        DefaultKafkaProducer producer = new DefaultKafkaProducer(bootstrapServers, env, config);
        producers.put(config.getProducerId(), producer);
    }

    /**
     * 启动所有消费者
     */
    public void startAllProducer() {
        for (Map.Entry<String, DefaultKafkaProducer> entry : producers.entrySet()) {
            try {
                entry.getValue().start();
            } catch (Exception e) {
                throw new RuntimeException("start producer error");
            }
            log.info("KafkaProducer : " + entry.getKey() + " is start");
        }
        log.info("All KafkaProducer is start");
    }

    /**
     * 停止所有消费者
     */
    public void stopAllProducer() {
        log.info("stopAllProducer start");
        for (Map.Entry<String, DefaultKafkaProducer> entry : producers.entrySet()) {
            try {
                entry.getValue().stop();
            } catch (Exception e) {
                throw new RuntimeException("stop producer error");
            }
            log.info("KafkaProducer : " + entry.getKey() + " is stop");
        }
        log.info("All KafkaProducer is stop");
    }
}
