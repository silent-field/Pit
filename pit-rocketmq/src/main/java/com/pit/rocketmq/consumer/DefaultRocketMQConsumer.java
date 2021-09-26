package com.pit.rocketmq.consumer;

import com.pit.rocketmq.config.ConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragelyByCircle;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Slf4j
public class DefaultRocketMQConsumer {
    private String topic;
    private String envSuffix;
    private String groupName;
    private DefaultMQPushConsumer consumer;
    private ConsumerConfig config;

    /**
     * 初始化配置文件里面的各种消费者属性配置
     * @param
     */
    public DefaultRocketMQConsumer(String nameServer, String env, ConsumerConfig config) {
        this.config = config;

        if (StringUtils.isBlank(config.getTopic())) {
            log.error("rocketMQConsumer's getConsumerId is null, init failed");
            return;
        }

        if (StringUtils.isBlank(config.getGroupName())) {
            log.error("rocketMQConsumer's getGroupId is null, init failed");
            return;
        }

        if (StringUtils.isBlank(nameServer)) {
            log.error("rocketMQConsumer's getNamesrvAddr is null, init failed");
            return;
        }

        if (CollectionUtils.isEmpty(config.getTopicAndTagMap())) {
            log.error("rocketMQConsumer's getTopicAndTagMap is null, init failed");
            return;
        }

        this.topic = config.getTopic();
        this.envSuffix = StringUtils.isBlank(env) ? "" : "_" + env;
        this.groupName = config.getGroupName();

        consumer = new DefaultMQPushConsumer(groupName + envSuffix);
        consumer.setNamesrvAddr(nameServer);
        consumer.setInstanceName(System.currentTimeMillis() + "");
        consumer.setConsumerGroup(groupName);
        if (StringUtils.isNotBlank(config.getUnitName())) {
            consumer.setUnitName(config.getUnitName());
        }
        subscribe(config.getTopicAndTagMap());

        //设置负载均衡策略
        AllocateMessageQueueStrategy allocateMessageQueueStrategy = new AllocateMessageQueueAveragelyByCircle();
        consumer.setAllocateMessageQueueStrategy(allocateMessageQueueStrategy);

        configureConsumer();
    }

    /**
     * 配置线程数等，不配置使用默认
     */
    void configureConsumer() {
        if (config != null) {
            //设置消费者其它参数
            String consumeFromWhere = config.getConsumeFromWhere();
            if (StringUtils.isNotBlank(consumeFromWhere)) {
                if (StringUtils.equals(consumeFromWhere, "CONSUME_FROM_LAST_OFFSET")) {
                   consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
                } else if (StringUtils.equals(consumeFromWhere, "CONSUME_FROM_FIRST_OFFSET")) {
                    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                } else if (StringUtils.equals(consumeFromWhere, "CONSUME_FROM_TIMESTAMP")) {
                    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
                }
            }

            // 消费线程池最小线程数 默认10
            if (config.getConsumeThreadMin() != null && config.getConsumeThreadMin() > 0) {
                consumer.setConsumeThreadMin(config.getConsumeThreadMin());
            }
            // 消费线程池最大线程数 默认20
            if (config.getConsumeThreadMax() != null && config.getConsumeThreadMax() > 0) {
                consumer.setConsumeThreadMax(config.getConsumeThreadMax());
            }
            // 重试次数
            if (config.getMaxReConsumeTimes() != null && config.getMaxReConsumeTimes() >= 0) {
                consumer.setMaxReconsumeTimes(config.getMaxReConsumeTimes());
            }

            if (config.getPullThresholdForQueue() != null && config.getPullThresholdForQueue() >= 0) {
                consumer.setPullThresholdForQueue(config.getPullThresholdForQueue());
            }

            if (config.getConsumeMessageBatchMaxSize() != null && config.getConsumeMessageBatchMaxSize() >= 0) {
                consumer.setConsumeMessageBatchMaxSize(config.getConsumeMessageBatchMaxSize());
            }

            if (config.getPullBatchSize() != null && config.getPullBatchSize() >= 0) {
                consumer.setPullBatchSize(config.getPullBatchSize());
            }

            if (config.getPullInterval() != null && config.getPullInterval() >= 0) {
                consumer.setPullInterval(config.getPullInterval());
            }
        }
    }

    /**
     * 订阅指定主题，可能有多个topic而且多个tag
     */
    void subscribe(Map<String, String> topicAndTagMap) {
        for (Map.Entry<String, String> entry : topicAndTagMap.entrySet()) {
            String topic = entry.getKey();
            String tag = entry.getValue();

            try {
                consumer.subscribe(topic, tag);
            } catch (MQClientException e) {
                log.error("rocketMQConsumer's topic or tag is null , init failed");
            }
        }
    }

    public void registerMessageListener(MessageListenerOrderly listener){
        consumer.registerMessageListener(listener);
    }

    public void registerMessageListener(MessageListenerConcurrently listener){
        consumer.registerMessageListener(listener);
    }

    public void start() throws MQClientException {
        consumer.start();
    }

    public void shutdown() {
        consumer.shutdown();
    }
}
