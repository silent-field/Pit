package com.pit.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.Consul.Builder;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Consul client
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
@Slf4j
public class ConsulClient {

	private static ConsulClient consulClient = new ConsulClient();

	private static Consul consul;

	private final static int minIdle = 2;

	private ConsulClient() {
		super();
	}

	public static ConsulClient instance(String consulAddress) {
		if (null == consul) {
			consulClient.init(consulAddress);
		}

		return consulClient;
	}

	private synchronized void init(String consulAddress) {
		if (null != consul) {
			return;
		}
		try {
			Builder builder = Consul.builder();
			String[] consulAddrArr = consulAddress.split(",");
			for (String consulAddr : consulAddrArr) {
				builder.withHostAndPort(HostAndPort.fromString(consulAddr));
			}
			consul = builder.withPing(false).build();
		} catch (Exception e) {
			log.error("ConsulClient init error:", e);
		}
	}

	/**
	 * 服务获取
	 */
	public Set<String> serviceGet(String name) {
		HealthClient client = consul.healthClient();

		Set<String> result = new HashSet<String>();
		// 获取所有正常的服务（健康检测通过的）
		List<ServiceHealth> responses = client.getHealthyServiceInstances(name).getResponse();
		for (ServiceHealth sh : responses) {
			Service service = sh.getService();
			String ip = service.getAddress();
			if (StringUtils.isBlank(ip)) {
				continue;
			}
			int port = service.getPort();
			String server = ip + ":" + port;
			log.debug("consul get {}", server);
			result.add(server);
		}

		Set<String> resultTmp = new HashSet<>(result.size());
		resultTmp.addAll(result);

		if (resultTmp.size() > minIdle) {
			return resultTmp;
		}

		return result;
	}
}
