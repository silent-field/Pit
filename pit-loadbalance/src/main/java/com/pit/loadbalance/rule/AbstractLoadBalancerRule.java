package com.pit.loadbalance.rule;

import com.pit.loadbalance.ILoadBalancer;
import com.pit.loadbalance.event.RequestExceptionFailEvent;
import com.pit.loadbalance.event.RequestSuccessEvent;
import com.pit.loadbalance.meta.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 负载均衡策略抽象类
 *
 * @Author gy
 * @Date 2020-03-20 16:41
 */
@Slf4j
public abstract class AbstractLoadBalancerRule implements IRule {
    private ILoadBalancer loadBalancer;

    public AbstractLoadBalancerRule(ILoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public void setLoadBalancer(ILoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public ILoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    @Override
    public ServiceMetaInfo.NodeMetaInfo choose(Object key) {
        if (!commonCheck()) {
            return null;
        }

        return choose(getLoadBalancer(), key);
    }

    /**
     * 抽取出来，便于进行UT
     *
     * @param lb
     * @param key
     * @return
     */
    public abstract ServiceMetaInfo.NodeMetaInfo choose(ILoadBalancer lb, Object key);

    /**
     * 通用检查
     *
     * @return
     */
    boolean commonCheck() {
        if (null == getLoadBalancer()) {
            log.warn("No associated load balancer");
            return false;
        }

        ServiceMetaInfo serviceMetaInfo = getLoadBalancer().getServiceMetaInfo();
        if (null == serviceMetaInfo) {
            log.warn("not exist ServiceMetaInfo");
            return false;
        }

        if (CollectionUtils.isEmpty(serviceMetaInfo.getClusterNetworkMetaInfo())) {
            log.warn("ServiceMetaInfo.clusterNetworkMetaInfo is empty");
            return false;
        }

        return true;
    }

    @Override
    public void whenRequestFail(RequestExceptionFailEvent event) {

    }

    @Override
    public void whenRequestSuccess(RequestSuccessEvent event) {

    }
}