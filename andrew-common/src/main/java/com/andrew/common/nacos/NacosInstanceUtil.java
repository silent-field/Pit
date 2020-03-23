package com.andrew.common.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 操作nacos注册中心：注册 或 从nacos获取一个可用实例
 *
 * @author Andrew
 * @date 2020/3/20
 */
@Slf4j
public class NacosInstanceUtil {
	private NamingService namingService = null;

	public NacosInstanceUtil(String nacosAddr) throws NacosException {
		namingService = NamingFactory.createNamingService(nacosAddr);
	}

	/**
	 * 注册实例
	 *
	 * @param nacosAddr
	 * @param appName
	 * @param appGroup
	 * @param ip
	 * @param port
	 * @throws NacosException
	 */
	public static void registerInstance(String nacosAddr, String appName, String appGroup, String ip, String port)
			throws NacosException {
		if (StringUtils.isAnyBlank(nacosAddr, appName, appGroup, ip, port)) {
			log.error("nacos discovery can not find nacosAddr/appName/appGroup/ip/port");
			return;
		}

		NamingService naming = NamingFactory.createNamingService(nacosAddr);
		naming.registerInstance(appName, appGroup, ip, Integer.parseInt(port));
	}

	/**
	 * 选择一个实例
	 *
	 * @param serviceName
	 * @param appGroup
	 * @return
	 */
	public Instance chooseInstance(String serviceName, String appGroup) {
		try {
			Instance instance = namingService.selectOneHealthyInstance(serviceName, appGroup);

			log.info("selected instance = {}", instance);
			return instance;
		} catch (NacosException e) {
			log.error("NacosInstanceSelector chooseInstance occurs exception", e);
			return null;
		}
	}
}
