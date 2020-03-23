package com.andrew.common.loadbalance.rule.impl;

import com.andrew.common.loadbalance.ILoadBalancer;
import com.andrew.common.loadbalance.meta.ServiceMetaInfo;
import com.andrew.common.loadbalance.rule.AbstractLoadBalancerRule;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询策略
 *
 * @Author Andrew
 * @Date 2019-06-14 15:51
 */
public class RoundRule extends AbstractLoadBalancerRule {
	// 调用计数器
	private AtomicInteger nextServerCounter = new AtomicInteger(0);

	public RoundRule(ILoadBalancer loadBalancer) {
		super(loadBalancer);
	}

	/**
	 *
	 * @param lb
	 * @param key
	 * @return
	 */
	public ServiceMetaInfo.NodeMetaInfo choose(ILoadBalancer lb, Object key) {
		List<ServiceMetaInfo.NodeMetaInfo> clusterNetworkMetaInfo = lb.getServiceMetaInfo().getClusterNetworkMetaInfo();
		int nextServerIndex = getNextNodeIndex(clusterNetworkMetaInfo.size());
		return clusterNetworkMetaInfo.get(nextServerIndex);
	}

	/**
	 * 轮询得到Node下标，不能使用nextServerCounter.incrementAndGet % module，存在溢出风险
	 * @param modulo
	 * @return
	 */
	public int getNextNodeIndex(int modulo) {
		for (; ; ) {
			int current = nextServerCounter.get();
			int next = (current + 1) % modulo;
			if (nextServerCounter.compareAndSet(current, next)) {
				return next;
			}
		}
	}
}
