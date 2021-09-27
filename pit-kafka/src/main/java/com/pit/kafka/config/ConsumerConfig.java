package com.pit.kafka.config;

import lombok.Data;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Data
public class ConsumerConfig {
    /**
     * 消费者Id
     */
    private String consumerId;
    private String topic;
    private String groupId;
    private boolean autoCommit = true;

    private String offsetReset;

    private Long backlogThreshold;

    /**
     * 告警检查间隔
     */
    private Integer checkInterval;

    /**
     * 核心线程数量
     */
    private Integer corePoolSize = 2;

    /**
     * 最大线程数量
     */
    private Integer maxPoolSize = 20;

    /**
     * 队列长度
     */
    private Integer queueCapacity = 100;

    /**
     * earliest     当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费
     * latest       当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据
     * none         topic各分区都存在已提交的offset时，从offset后开始消费；只要有一个分区不存在已提交的offset，则抛出异常
     */
    private String offset;
}
