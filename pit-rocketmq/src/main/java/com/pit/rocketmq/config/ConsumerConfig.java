package com.pit.rocketmq.config;

import lombok.Data;

import java.util.Map;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Data
public class ConsumerConfig {
    private String topic;
    /**
     * rocketMq消费者组名
     */
    private String groupName;
    /**
     * 消费者订阅的多个主题和tag，一个主题下有多个tag可以用 || 分割，例如：TagA || TagC || TagD
     */
    private Map<String, String> topicAndTagMap;

    private String instanceName;
    private String unitName;

    /**
     * 消费者是否要进行有序消费
     */
    private boolean orderly;

    /**
     * 消费线程池最小线程数 默认10
     */
    private Integer consumeThreadMin;
    /**
     * 消费线程池最大线程数 默认20
     */
    private Integer consumeThreadMax;
    /**
     * 重试次数
     */
    private Integer maxReConsumeTimes;

    private Integer pullThresholdForQueue;

    private Integer consumeMessageBatchMaxSize;

    private Integer pullBatchSize;

    private Integer pullInterval;

    private String consumeFromWhere = "CONSUME_FROM_LAST_OFFSET";

    /**
     * 消费积压阈值
     */
    private int backlogThreshold;

    /**
     * 告警检查间隔
     */
    private int alarmCheckInterval;

}
