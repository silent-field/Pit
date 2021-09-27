package com.pit.kafka;

import com.pit.kafka.config.KafkaProperties;
import com.pit.kafka.consumer.MessageConsumer;
import com.pit.kafka.factory.KafkaFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/27
 */
@Configuration
@ConditionalOnClass(KafkaFactory.class)
@EnableConfigurationProperties({KafkaProperties.class})
@Slf4j
public class KafkaAutoConfig implements ApplicationContextAware {
    @Autowired
    private KafkaProperties kafkaProperties;

    @Value("spring.profiles.active:")
    private String env;

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "kafka.config", name = "enabled", havingValue = "true")
    public KafkaFactory kafkaFactory() {
        KafkaFactory kafkaFactory = new KafkaFactory();
        // 处理消费者
        if (CollectionUtils.isNotEmpty(kafkaProperties.getConsumers())) {
            final Map<String, MessageConsumer> messageConsumerMap = applicationContext.getBeansOfType(MessageConsumer.class);
            // 转换成以topic为key
            Map<String, MessageConsumer> consumerMap = messageConsumerMap.values().stream().collect(Collectors.toMap(MessageConsumer::topic, Function.identity()));
            kafkaProperties.getConsumers().forEach(mqConsumerConfiguration -> {
                MessageConsumer messageConsumer = consumerMap.get(mqConsumerConfiguration.getTopic());
                if (Objects.isNull(messageConsumer)) {
                    log.error("Kafka consumer init fail, messageConsumer is null! consumerId: " + mqConsumerConfiguration.getConsumerId());
                    return;
                }
                kafkaFactory.createConsumer(kafkaProperties.getBootstrapServers(), env, mqConsumerConfiguration, messageConsumer);
            });
        }

        //处理生产者
        if (CollectionUtils.isNotEmpty(kafkaProperties.getProducers())) {
            kafkaProperties.getProducers().forEach(kafkaProducerConfiguration -> {
                kafkaFactory.createProducer(kafkaProperties.getBootstrapServers(), env, kafkaProducerConfiguration);
            });
        }

        return kafkaFactory;
    }

    @Bean
    @ConditionalOnBean({KafkaFactory.class})
    public KafkaRunner kafkaRunner(KafkaFactory kafkaFactory) {
        return new KafkaRunner(kafkaFactory);
    }

    @Bean
    @ConditionalOnBean({KafkaFactory.class})
    public KafkaStopListener kafkaStop(KafkaFactory kafkaFactory) {
        return new KafkaStopListener(kafkaFactory);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
