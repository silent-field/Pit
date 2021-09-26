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

    private String groupName;
    /**
     * 发送消息超时时间，单位毫秒
     */
    private Integer sendMsgTimeout = 3000;
    /**
     * 限制的消息大小，默认131072（128kb），设为-1表示不限制
     */
    private Integer maxMessageSize;
    /**
     * 是否重试，大于0生效
     */
    private Integer retryTimesWhenSendFailed;
}
