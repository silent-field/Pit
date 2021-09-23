package com.pit.rocketmq.config;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Data
@ConfigurationProperties(prefix = "rocketmq.config")
@Component
public class RocketMQProperties {
    /** RocketMQ namesrvAddr */
    private String nameServer;

    private List<ConsumerConfig> consumers;

    private List<ProducerConfig> producers;

    private Map<String, ProducerConfig> producerMap = new HashMap<>();
    private Map<String, ConsumerConfig> consumerMap = new HashMap<>();

    @PostConstruct
    public void convert() {
        if (CollectionUtils.isNotEmpty(producers)) {
            producerMap = producers.stream()
                    .collect(Collectors.toMap(ProducerConfig::getTopic, o -> o));
        }

        if (CollectionUtils.isNotEmpty(consumers)) {
            consumerMap = consumers.stream()
                    .collect(Collectors.toMap(ConsumerConfig::getTopic, o -> o));
        }
    }

    public ProducerConfig getProducerConfig(String topic) {
        if (producerMap == null) {
            return null;
        }
        return producerMap.get(topic);
    }

    public ConsumerConfig getConsumerConfig(String topic) {
        if (consumerMap == null) {
            return null;
        }
        return consumerMap.get(topic);
    }
}
