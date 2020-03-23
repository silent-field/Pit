package com.andrew.common.mq.kafka.autoware;

import com.andrew.common.mq.kafka.config.KafkaConfigurationsProperties;
import com.andrew.common.mq.kafka.config.KafkaConsumerConfiguration;
import com.andrew.common.mq.kafka.config.KafkaProducerConfiguration;
import com.andrew.common.mq.kafka.consumer.IKafkaMsgProcessor;
import com.andrew.common.mq.kafka.factory.KafkaFactory;
import com.andrew.common.mq.rocketmq.listener.IRocketMQMsgProcessor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * RocketMQ自动配置类，在spring开始加载的时候就启动，可以在配置文件里设置enable关闭
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
@Configuration
@ConditionalOnClass(KafkaFactory.class)
@EnableConfigurationProperties({KafkaConfigurationsProperties.class})
public class KafkaAutoConfig implements ApplicationContextAware {

	@Autowired
	private KafkaConfigurationsProperties kafkaConfigurationsProperties;

	@Value("spring.application.name:")
	private String appName;

	private ApplicationContext applicationContext;

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "kafka.config", name = "enabled", havingValue = "true")
	KafkaFactory kafkaFactory() {
		KafkaFactory kafkaFactory = KafkaFactory.getInstance();
		// 创建生产者
		if (CollectionUtils.isNotEmpty(kafkaConfigurationsProperties.getProducers())) {
			for (KafkaProducerConfiguration config : kafkaConfigurationsProperties.getProducers()) {
				kafkaFactory.createProducer(config);
			}
		}

		//处理消费者
		if (CollectionUtils.isNotEmpty(kafkaConfigurationsProperties.getConsumers())) {
			for (KafkaConsumerConfiguration config : kafkaConfigurationsProperties.getConsumers()) {
				//处理processor
				final Map<String, IKafkaMsgProcessor> processorMap = applicationContext.getBeansOfType(IKafkaMsgProcessor.class);
				List<IKafkaMsgProcessor> list = new ArrayList<>();
				if (MapUtils.isNotEmpty(processorMap)) {
					processorMap.forEach((key, value) -> {
						IKafkaMsgProcessor kafkaMsgProcessor = value;
						if (config.getConsumerId().equals(kafkaMsgProcessor.bindConsumerId())) {
							try {
								list.add(kafkaMsgProcessor);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					});
				}
				if (StringUtils.isBlank(appName)) {
					appName = "defaultAppName";
				}
				config.setAppName(appName);
				kafkaFactory.createConsumer(config, list);
			}
		}
		return kafkaFactory;
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
