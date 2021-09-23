package com.pit.rocketmq.config;

import lombok.Data;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Data
public class ConsumerConfig {
    private String topic;
    private Integer consumeThreadMin;
    private Integer consumeThreadMax;
    private Integer maxReConsumeTimes;
    private String consumeFromWhere = "CONSUME_FROM_LAST_OFFSET";
}
