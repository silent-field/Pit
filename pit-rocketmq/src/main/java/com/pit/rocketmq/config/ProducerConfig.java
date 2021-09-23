package com.pit.rocketmq.config;

import lombok.Data;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Data
public class ProducerConfig {
    private String topic;
    private Integer sendMsgTimeout;
    private Integer retryTimesWhenSendFailed;
}
