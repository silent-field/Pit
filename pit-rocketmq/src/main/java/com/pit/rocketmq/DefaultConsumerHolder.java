package com.pit.rocketmq;

import com.pit.rocketmq.config.ConsumerConfig;
import com.pit.rocketmq.config.RocketMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Slf4j
public abstract class DefaultConsumerHolder implements Closeable, ApplicationListener<ContextClosedEvent> {
    @Autowired
    protected RocketMQProperties rocketMQProperties;

    protected String env;

    protected String envSuffix;

    protected String groupName;

    protected String topic;

    protected String tagExpression;

    protected DefaultMQPushConsumer consumer;

    protected ConsumerConfig consumerConfig;

    protected List<SubscriptionData> subscriptionData = new LinkedList<>();

    protected Set<String> topics = new HashSet<>();

    public DefaultConsumerHolder(String env, String groupName, String topic, String tagExpression) {
        this.env = env;
        this.envSuffix = StringUtils.isBlank(env) ? "" : "_" + env;
        this.topic = topic;
        this.groupName = groupName;
        this.tagExpression = tagExpression;
        this.topics.add(topic + envSuffix);
        subscriptionData.add(toSubData(topic, tagExpression));
    }

    public DefaultConsumerHolder(String env, String groupName, List<SubscriptionData> subscriptionData) {
        this.env = env;
        this.envSuffix = StringUtils.isBlank(env) ? "" : "_" + env;
        this.groupName = groupName;
        this.topic = subscriptionData.get(0).getTopic();
        this.tagExpression = subscriptionData.get(0).getSubString();
        this.subscriptionData.addAll(subscriptionData);
        this.topics.addAll(subscriptionData.stream().map(x -> x.getTopic() + envSuffix).collect(Collectors.toSet()));
    }

    protected SubscriptionData toSubData(String topic) {
        return toSubData(topic, "*");
    }

    protected SubscriptionData toSubData(String topic, String tagExpression) {
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setTopic(topic);
        subscriptionData.setSubString(tagExpression);
        return subscriptionData;
    }

    // --------------- init
    @PostConstruct
    protected void initConsumer() {
        try {
            consumerConfig = rocketMQProperties.getConsumerConfig(topic);
            // 是否进行初始化配置：不配置默认初始化
            if (Objects.nonNull(consumerConfig)) {
                log.info("initConsumer consumerConfig:{} groupName:{},subscriptionData:{},nameServer:{}",
                        consumerConfig, groupName + envSuffix, subscriptionData, rocketMQProperties.getNameServer());
                return;
            }
            consumer = new DefaultMQPushConsumer(groupName + envSuffix);
            consumer.setInstanceName(System.currentTimeMillis() + "");
            consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
            subscriptionData.forEach(it -> {
                try {
                    consumer.subscribe(it.getTopic() + envSuffix, Optional.ofNullable(it.getSubString()).orElse("*"));
                } catch (MQClientException ex) {
                    log.error("initConsumer_fail! groupName:{},topic:{},tags:{},nameServer:{}", groupName + envSuffix, it.getTopic() + envSuffix,
                            it.getSubString(), rocketMQProperties.getNameServer(), ex);
                }
            });
            // 配置线程数等
            configureConsumer();
            // 固定单条处理，降低复杂度
            consumer.setConsumeMessageBatchMaxSize(1);
            consumer.registerMessageListener(getConsumerLister());
            consumer.setConsumeFromWhere(null == consumerConfig ? ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET : ConsumeFromWhere.valueOf(consumerConfig.getConsumeFromWhere()));
            consumer.start();
            log.info("initConsumer_success. consumerConfig:{} groupName:{},subscriptionData:{},nameServer:{}",
                    consumerConfig, groupName + envSuffix, subscriptionData, rocketMQProperties.getNameServer());
        } catch (MQClientException e) {
            log.error("initConsumer_fail! groupName:{},subscriptionData:{},nameServer:{}",
                    groupName + envSuffix, subscriptionData, rocketMQProperties.getNameServer(), e);
        }
    }

    /**
     * 配置线程数等，不配置使用默认
     */
    protected void configureConsumer() {
        if (consumerConfig != null) {
            // 消费线程池最小线程数 默认10
            if (consumerConfig.getConsumeThreadMin() != null && consumerConfig.getConsumeThreadMin() > 0) {
                consumer.setConsumeThreadMin(consumerConfig.getConsumeThreadMin());
            }
            // 消费线程池最大线程数 默认20
            if (consumerConfig.getConsumeThreadMax() != null && consumerConfig.getConsumeThreadMax() > 0) {
                consumer.setConsumeThreadMax(consumerConfig.getConsumeThreadMax());
            }
            // 重试次数
            if (consumerConfig.getMaxReConsumeTimes() != null && consumerConfig.getMaxReConsumeTimes() >= 0) {
                consumer.setMaxReconsumeTimes(consumerConfig.getMaxReConsumeTimes());
            }
        }
    }

    protected MessageListenerConcurrently getConsumerLister() {
        return (msgList, consumeConcurrentlyContext) -> {
            if (CollectionUtils.isEmpty(msgList)) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            MessageExt messageExt = msgList.get(0);
            try {
                log.info("consumeMessage topic:{} msg:{}", messageExt.getTopic(), new String(messageExt.getBody()));
                if (topics.contains(messageExt.getTopic())) {
                    return consumeMsg(messageExt);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                log.error("consumeMessage fail topic:{}", messageExt.getTopic(), e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        };
    }

    abstract ConsumeConcurrentlyStatus consumeMsg(MessageExt msg);

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        close();
    }

    @Override
    public void close() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("shutdownConsumer!, subscriptionData:{}", subscriptionData);
        }
    }
}
