package com.pit.rocketmq.config;

import com.pit.rocketmq.RocketMQRunner;
import com.pit.rocketmq.RocketMQStopListener;
import com.pit.rocketmq.factory.RocketMQFactory;
import com.pit.rocketmq.listener.IRocketMQMsgHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.rocketmq.client.log.ClientLogger;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Configuration
@ConditionalOnClass(RocketMQFactory.class)
@EnableConfigurationProperties({RocketMQProperties.class})
@Slf4j
public class RocketMqAutoConfig implements ApplicationContextAware {
    @Autowired
    private RocketMQProperties rocketMQProperties;

    @Value("spring.application.name:")
    private String appName;

    @Value("application.profile:")
    private String env;

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rocketmq.config", name = "enabled", havingValue = "true")
    RocketMQFactory rocketMqFactory() {
        System.setProperty(ClientLogger.CLIENT_LOG_USESLF4J, "true");

        RocketMQFactory rocketMqFactory = new RocketMQFactory();

        // 通过生产者配置创建生产者
        if (CollectionUtils.isNotEmpty(rocketMQProperties.getProducers())) {
            rocketMQProperties.getProducers().forEach(config -> {
                rocketMqFactory.createProducer(rocketMQProperties.getNameServer(), env, config);
            });
        }

        // 通过消费者配置创建消费者
        if (CollectionUtils.isNotEmpty(rocketMQProperties.getConsumers())) {
            final Map<String, IRocketMQMsgHandler> iProcessorMap = applicationContext.getBeansOfType(IRocketMQMsgHandler.class);

            rocketMQProperties.getConsumers().forEach(consumerConfig -> {
                //处理processor
                List<IRocketMQMsgHandler> list = new ArrayList<>();
                if (MapUtils.isNotEmpty(iProcessorMap)) {
                    iProcessorMap.forEach((key, value) -> {
                        IRocketMQMsgHandler rocketMQMsgHandler = value;
                        if (consumerConfig.getTopic().equals(rocketMQMsgHandler.topic())) {
                            try {
                                list.add(rocketMQMsgHandler);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }

                if (CollectionUtils.isEmpty(list)) {
                    log.error("RocketMQ consumer init fail, processList empty! topic: " + consumerConfig.getTopic());
                    return;
                }

                rocketMqFactory.createConsumer(rocketMQProperties.getNameServer(), env, consumerConfig, list);
            });
        }
        return rocketMqFactory;
    }

    @Bean
    @ConditionalOnBean({RocketMQFactory.class})
    public RocketMQRunner rocketMqRunner(RocketMQFactory rocketMQFactory) {
        return new RocketMQRunner(rocketMQFactory);
    }

    @Bean
    @ConditionalOnBean({RocketMQFactory.class})
    public RocketMQStopListener rocketMqStopListener(RocketMQFactory rocketMQFactory) {
        return new RocketMQStopListener(rocketMQFactory);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
