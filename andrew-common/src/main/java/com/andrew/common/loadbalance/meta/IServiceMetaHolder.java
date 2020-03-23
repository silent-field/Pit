package com.andrew.common.loadbalance.meta;

public interface IServiceMetaHolder {
	ServiceMetaInfo getServiceMetaInfo(String symbol);

	void destroy();
}
