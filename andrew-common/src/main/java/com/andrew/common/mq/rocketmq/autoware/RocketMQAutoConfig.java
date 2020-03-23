package com.andrew.common.mq.rocketmq.autoware;

import com.andrew.common.mq.rocketmq.config.RocketMQConfigurationsProperties;
import com.andrew.common.mq.rocketmq.factory.RocketMQFactory;
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
@ConditionalOnClass(RocketMQFactory.class)
@EnableConfigurationProperties({RocketMQConfigurationsProperties.class})
public class RocketMQAutoConfig implements ApplicationContextAware {

	@Autowired
	private RocketMQConfigurationsProperties rocketMQConfigurationsProperties;

	@Value("spring.application.name:")
	private String appName;

	private ApplicationContext applicationContext;

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "rocketmq.config", name = "enabled", havingValue = "true")
	RocketMQFactory rocketMqFactory() {
		RocketMQFactory rocketMqFactory = new RocketMQFactory();
		//处理生产者
		if (CollectionUtils.isNotEmpty(rocketMQConfigurationsProperties.getProducers())) {
			rocketMQConfigurationsProperties.getProducers().forEach(config -> {
				rocketMqFactory.createProducer(config);
			});
		}

		//处理消费者
		if (CollectionUtils.isNotEmpty(rocketMQConfigurationsProperties.getConsumers())) {
			rocketMQConfigurationsProperties.getConsumers().forEach(mqConsumerConfiguration -> {
				//处理processor
				final Map<String, IRocketMQMsgProcessor> iProcessorMap = applicationContext.getBeansOfType(IRocketMQMsgProcessor.class);
				List<IRocketMQMsgProcessor> list = new ArrayList<>();
				if (MapUtils.isNotEmpty(iProcessorMap)) {
					iProcessorMap.forEach((key, value) -> {
						IRocketMQMsgProcessor iRocketMQMsgProcessor = value;
						if (mqConsumerConfiguration.getConsumerId().equals(iRocketMQMsgProcessor.bindConsumerId())) {
							try {
								list.add(iRocketMQMsgProcessor);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					});
				}
				if (StringUtils.isBlank(appName)) {
					appName = "defaultAppName";
				}
				mqConsumerConfiguration.setAppName(appName);
				rocketMqFactory.createConsumer(mqConsumerConfiguration, list);

			});
		}
		return rocketMqFactory;
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
