package com.pit.loadbalance.rule.impl;

import com.google.common.hash.Hashing;
import com.pit.loadbalance.ILoadBalancer;
import com.pit.loadbalance.meta.ServiceMetaInfo;
import com.pit.loadbalance.rule.AbstractLoadBalancerRule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 一致性Hash策略
 *
 * @Author gy
 * @Date 2020-03-20 15:51
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
        // 使用Guava的一致性哈希算法
        int selectedIndex = Hashing.consistentHash(hashcode, nodeCount);

        return clusterNetworkMetaInfo.get(selectedIndex);
    }
}