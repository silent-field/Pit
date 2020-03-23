package com.andrew.common.loadbalance.rule.impl;


import com.andrew.common.loadbalance.ILoadBalancer;
import com.andrew.common.loadbalance.meta.ServiceMetaInfo;
import com.andrew.common.loadbalance.rule.AbstractLoadBalancerRule;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机策略
 *
 * @Author Andrew
 * @Date 2019-06-14 15:51
 */
public class RandomRule extends AbstractLoadBalancerRule {
	public RandomRule(ILoadBalancer loadBalancer) {
		super(loadBalancer);
	}

	public ServiceMetaInfo.NodeMetaInfo choose(ILoadBalancer lb, Object key) {
		List<ServiceMetaInfo.NodeMetaInfo> clusterNetworkMetaInfo = lb.getServiceMetaInfo().getClusterNetworkMetaInfo();
		int nextServerIndex = chooseRandomInt(clusterNetworkMetaInfo.size());
		return clusterNetworkMetaInfo.get(nextServerIndex);
	}

	protected int chooseRandomInt(int serverCount) {
		return ThreadLocalRandom.current().nextInt(serverCount);
	}
}
