package com.andrew.common.loadbalance.meta;

/**
 * @Description
 *
 * @Author Andrew
 * @Date 2019-07-31 15:14
 */
public class EmptyServiceMetaInfoHolder implements IServiceMetaHolder {
	@Override
	public ServiceMetaInfo getServiceMetaInfo(String symbol) {
		return new ServiceMetaInfo();
	}

	@Override
	public void destroy() {

	}
}
