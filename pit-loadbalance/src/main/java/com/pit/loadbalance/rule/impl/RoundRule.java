package com.pit.loadbalance.rule.impl;

import com.pit.loadbalance.ILoadBalancer;
import com.pit.loadbalance.meta.ServiceMetaInfo;
import com.pit.loadbalance.rule.AbstractLoadBalancerRule;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询策略
 *
 * @Author gy
 * @Date 2020-03-20 15:51
 */
public class RoundRule extends AbstractLoadBalancerRule {
    /**
     * 调用计数器
     */
    private AtomicInteger nextServerCounter = new AtomicInteger(0);

    public RoundRule(ILoadBalancer loadBalancer) {
        super(loadBalancer);
    }

    /**
     * @param lb
     * @param key
     * @return
     */
    @Override
    public ServiceMetaInfo.NodeMetaInfo choose(ILoadBalancer lb, Object key) {
        List<ServiceMetaInfo.NodeMetaInfo> clusterNetworkMetaInfo = lb.getServiceMetaInfo().getClusterNetworkMetaInfo();
        int nextServerIndex = getNextNodeIndex(clusterNetworkMetaInfo.size());
        return clusterNetworkMetaInfo.get(nextServerIndex);
    }

    /**
     * 轮询得到Node下标，不能直接使用 nextServerCounter.incrementAndGet % module，存在溢出风险
     * <p>
     * 也可以增长到一定的数值时，将nextServerCounter设置为0，从0计数
     *
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
