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
}
