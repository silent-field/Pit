package com.andrew.common.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * 初始化nacos配置监听
 *
 * @author Andrew
 * @date 2020/3/20
 */
@Slf4j
public class NacosInitor {
	public static final String DEV_ENV = "DEV_GROUP";

	private static boolean init = false;

	private NacosInitor() {
	}

	public synchronized static void init(String nacosAddr, String app, String env, Listener listener) throws Exception {
		if (!init) {
			try {
				initConfig(nacosAddr, app, env, listener);
			} catch (Exception e) {
				log.error("init config error:", e);
				e.printStackTrace();
				throw e;
			}
		}
	}

	private static void initConfig(String nacosAddr, String appName, String env, Listener listener) throws NacosException {
		String appGroup = (StringUtils.isBlank(env)) ? DEV_ENV : env;
		Properties properties = new Properties();
		properties.put("serverAddr", nacosAddr);
		ConfigService configService = NacosFactory.createConfigService(properties);
		String content = configService.getConfig(appName, appGroup, 5000);
		log.info("nacos config info:" + content);

		if (null != listener) {
			configService.addListener(appName, appGroup, listener);
		}
	}
}
