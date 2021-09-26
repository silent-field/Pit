package com.pit.rocketmq.factory;

import com.pit.rocketmq.config.ConsumerConfig;
import com.pit.rocketmq.config.ProducerConfig;
import com.pit.rocketmq.consumer.DefaultRocketMQConsumer;
import com.pit.rocketmq.listener.CurrentlyRocketMQMessageListener;
import com.pit.rocketmq.listener.IRocketMQMsgHandler;
import com.pit.rocketmq.listener.OrderlyRocketMQMessageListener;
import com.pit.rocketmq.producer.DefaultRocketMQProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Slf4j
public class RocketMQFactory {
    /**
     * 用于存放已经存在的消费者map组,用concurrentHashMap保证并发性
     */
    private volatile Map<String, DefaultRocketMQConsumer> consumers = new ConcurrentHashMap<>();

    private volatile Map<String, DefaultRocketMQProducer> producers = new ConcurrentHashMap<>();

    /**
     * 创建一个生产者
     */
    public synchronized DefaultRocketMQProducer createProducer(String nameServer, String env, ProducerConfig config) {
        if (producers.containsKey(config.getTopic())) {
            return producers.get(config.getTopic());
        }

        try {
            DefaultRocketMQProducer producer = new DefaultRocketMQProducer(nameServer, env, config);
            producers.put(config.getTopic(), producer);
            log.info("MQProducer init success, detail :{}" + config);

            return producer;
        } catch (Exception e) {
            log.error("MQProducer start error : " + config, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个消费者
     */
    public DefaultRocketMQConsumer createConsumer(String nameServer, String env, ConsumerConfig config, List<IRocketMQMsgHandler> list) {
        //如果Map里面存在就直接放回，不重复创建消费者
        if (consumers.containsKey(config.getTopic())) {
            return consumers.get(config.getTopic());
        }

        try {
            //根据消费者配置文件初始化消费者
            DefaultRocketMQConsumer consumer = new DefaultRocketMQConsumer(nameServer, env, config);

            //设置消费者回调类型
            if (config.isOrderly()) {
                //顺序消费类型
                OrderlyRocketMQMessageListener orderlyRocketMqMessageListener = new OrderlyRocketMQMessageListener();
                orderlyRocketMqMessageListener.setHandlers(list);
                consumer.registerMessageListener(orderlyRocketMqMessageListener);
            } else {
                //并发乱序消费类型
                CurrentlyRocketMQMessageListener currentlyRocketMqMessageListener = new CurrentlyRocketMQMessageListener();
                currentlyRocketMqMessageListener.setHandlers(list);
                consumer.registerMessageListener(currentlyRocketMqMessageListener);
            }

            consumers.put(config.getTopic(), consumer);
            log.info("rocketMQConsumer init success, detail : {}", config);
            log.info("rocketMQConsumer processors list size : {}", list.size());
            return consumer;
        } catch (Exception e) {
            log.error("rocketMQConsumer start error" + config.toString(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动所有消费者
     */
    public void startAllConsumer() {
        for (Map.Entry<String, DefaultRocketMQConsumer> entry : consumers.entrySet()) {
            try {
                entry.getValue().start();
            } catch (MQClientException e) {
                throw new RuntimeException("start consumer error");
            }
            log.info("RocketMQConsumer : " + entry.getKey() + " is start");
        }
        log.info("All RocketMQConsumer is start");
    }

    /**
     * 启动所有生产者
     */
    public void startAllProducer() {
        for (Map.Entry<String, DefaultRocketMQProducer> entry : producers.entrySet()) {
            try {
                entry.getValue().start();
            } catch (MQClientException e) {
                throw new RuntimeException("start producer error");
            }
            log.info("RocketMQProducer : " + entry.getKey() + " is start");
        }
        log.info("All RocketMQProducer is start");
    }

    /**
     * 停止所有消费者
     */
    public void stopAllConsumer() {
        for (Map.Entry<String, DefaultRocketMQConsumer> entry : consumers.entrySet()) {
            entry.getValue().shutdown();
            log.info("RocketMQConsumer :" + entry.getKey() + " is shutdown");
        }
        log.info("All RocketMQConsumer is shutdown");
    }

    /**
     * 停止所有生产者
     */
    public void stopAllProducer() {
        for (Map.Entry<String, DefaultRocketMQProducer> entry : producers.entrySet()) {
            entry.getValue().shutdown();
            log.info("RocketMQProducer :" + entry.getKey() + " is shutdown");
        }
        log.info("All RocketMQProducer is shutdown");
    }
}
