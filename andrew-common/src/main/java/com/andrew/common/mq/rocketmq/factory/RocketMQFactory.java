package com.andrew.common.mq.rocketmq.factory;

import com.andrew.common.mq.rocketmq.config.RocketMQConsumerConfiguration;
import com.andrew.common.mq.rocketmq.config.RocketMQProducerConfiguration;
import com.andrew.common.mq.rocketmq.consumer.RocketMQMessageConsumer;
import com.andrew.common.mq.rocketmq.listener.CurrentlyRocketMQMessageListener;
import com.andrew.common.mq.rocketmq.listener.IRocketMQMsgProcessor;
import com.andrew.common.mq.rocketmq.listener.OrderlyRocketMQMessageListener;
import com.andrew.common.mq.rocketmq.producer.RocketMqMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来集中管理所有的consumer线程池，包括创建和销毁
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Slf4j
public class RocketMQFactory {

    /**
     * 用于存放已经存在的消费者map组,用concurrentHashMap保证并发性
     */
    private static Map<String, RocketMQMessageConsumer> consumers = new ConcurrentHashMap<>();

    private static Map<String, RocketMqMessageProducer> producers = new ConcurrentHashMap<>();

    /**
     * 将工厂设置为单例模式
     */
    private static class SingletonHolder {
        static final RocketMQFactory instance = new RocketMQFactory();
    }

    public static RocketMQFactory getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 创建一个生产者
     */
    public RocketMqMessageProducer createProducer(RocketMQProducerConfiguration config) {
        if (producers.containsKey(config.getProducerId())) {
            return producers.get(config.getProducerId());
        }

        try {
            RocketMqMessageProducer producer = new RocketMqMessageProducer(config);
            producer.start();
            producers.put(config.getProducerId(), producer);
            log.info("MQProducer start success, detail :{}" + config.toString());

            return producer;
        } catch (MQClientException e) {
            log.error("MQProducer start error " + config.toString(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个消费者
     */
    public RocketMQMessageConsumer createConsumer(RocketMQConsumerConfiguration config, List<IRocketMQMsgProcessor> list) {
        //如果Map里面存在就直接放回，不重复创建消费者
        if (consumers.containsKey(config.getConsumerId())) {
            return consumers.get(config.getConsumerId());
        }

        try {
            //根据消费者配置文件初始化消费者
            RocketMQMessageConsumer consumer = new RocketMQMessageConsumer(config);

            //设置消费者回调类型
            if (config.isOrderly()) {
                //顺序消费类型
                OrderlyRocketMQMessageListener orderlyRocketMqMessageListener = new OrderlyRocketMQMessageListener(
                        config);
                orderlyRocketMqMessageListener.setProcessorList(list);
                consumer.registerMessageListener(orderlyRocketMqMessageListener);
            } else {
                //并发乱序消费类型
                CurrentlyRocketMQMessageListener currentlyRocketMqMessageListener = new CurrentlyRocketMQMessageListener(
                        config);
                currentlyRocketMqMessageListener.setProcessorList(list);
                consumer.registerMessageListener(currentlyRocketMqMessageListener);
            }

            consumer.start();
            consumers.put(config.getConsumerId(), consumer);
            log.info("rocketMQConsumer start success, detail :" + config.toString());
            log.info("rocketMQConsumer processors list size :" + list.size());
            return consumer;
        } catch (Exception e) {
            log.error("rocketMQConsumer start error" + config.toString(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取某个消费者
     */
    public RocketMQMessageConsumer getConsumer(String consumerId) {
        if (StringUtils.isNotBlank(consumerId) && consumers.containsKey(consumerId)) {
            return consumers.get(consumerId);
        }
        return null;
    }

    /**
     * 获取某个生产者
     */
    public RocketMqMessageProducer getProducer(String producerId) {
        if (StringUtils.isNotBlank(producerId) && producers.containsKey(producerId)) {
            return producers.get(producerId);
        }
        return null;
    }

    /**
     * 停止某个消费者
     */
    public void stopConsumer(String consumerId) {
        if (StringUtils.isNotBlank(consumerId) && consumers.containsKey(consumerId)) {
            consumers.get(consumerId).shutdown();
            consumers.remove(consumerId);
            log.info("RocketMQConsumer :" + consumerId + "is shutdown");
        }
    }

    /**
     * 停止某个生产者
     */
    public void stopProducer(String producerId) {
        if (StringUtils.isNotBlank(producerId) && producers.containsKey(producerId)) {
            producers.get(producerId).shutdown();
            producers.remove(producerId);
            log.info("RocketMQProducer :" + producerId + "is shutdown");
        }
    }


    /**
     * 停止所有消费者
     */
    public void stopAllConsumer() {
        for (Map.Entry<String, RocketMQMessageConsumer> entry : consumers.entrySet()) {
            entry.getValue().shutdown();
            log.info("RocketMQConsumer :" + entry.getKey() + "is shutdown");
        }
        log.info("All RocketMQConsumer is shutdown");
    }

    /**
     * 停止所有生产者
     */
    public void stopAllProducer() {
        for (Map.Entry<String, RocketMqMessageProducer> entry : producers.entrySet()) {
            entry.getValue().shutdown();
            log.info("RocketMQProducer :" + entry.getKey() + "is shutdown");
        }
        log.info("All RocketMQProducer is shutdown");
    }

}
