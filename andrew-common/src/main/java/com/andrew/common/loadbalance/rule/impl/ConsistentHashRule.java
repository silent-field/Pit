package com.andrew.common.loadbalance.rule.impl;

import com.andrew.common.loadbalance.ILoadBalancer;
import com.andrew.common.loadbalance.meta.ServiceMetaInfo;
import com.andrew.common.loadbalance.rule.AbstractLoadBalancerRule;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 一致性Hash策略
 *
 * @Author Andrew
 * @Date 2019-06-14 15:51
 */
@Slf4j
public class ConsistentHashRule extends AbstractLoadBalancerRule {
	public ConsistentHashRule(ILoadBalancer loadBalancer) {
		super(loadBalancer);
	}

	@Override
	public ServiceMetaInfo.NodeMetaInfo choose(ILoadBalancer lb, Object key) {
		List<ServiceMetaInfo.NodeMetaInfo> clusterNetworkMetaInfo = lb.getServiceMetaInfo().getClusterNetworkMetaInfo();
		int nodeCount = clusterNetworkMetaInfo.size();

		int hashcode = key.hashCode();
		int selectedIndex = Hashing.consistentHash(hashcode, nodeCount); // 使用Guava的一致性哈希算法

		return clusterNetworkMetaInfo.get(selectedIndex);
	}
}