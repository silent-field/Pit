package com.pit.loadbalance.meta;

/**
 * 应用服务器信息持有者
 *
 * @Author gy
 * @Date 2020-03-20 17:12
 */
public interface IServiceMetaHolder {
	/**
	 * 根据应用标识获取集群信息
	 * @param symbol
	 * @return
	 */
	ServiceMetaInfo getServiceMetaInfo(String symbol);

	/**
	 * 销毁操作
	 */
	void destroy();
}
