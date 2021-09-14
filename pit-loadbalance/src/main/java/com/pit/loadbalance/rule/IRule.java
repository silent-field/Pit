package com.pit.loadbalance.rule;

import com.pit.loadbalance.ILoadBalancer;
import com.pit.loadbalance.event.RequestExceptionFailEvent;
import com.pit.loadbalance.event.RequestSuccessEvent;
import com.pit.loadbalance.meta.ServiceMetaInfo;

/**
 * 负载均衡策略类
 *
 * @Author gy
 * @Date 2020-03-20 15:25
 */
public interface IRule {
    /**
     * 通过负载均衡策略选出一个节点
     * @param key
     * @return
     */
    ServiceMetaInfo.NodeMetaInfo choose(Object key);

    /**
     * 获取负载均衡器
     * @return
     */
    ILoadBalancer getLoadBalancer();

    /**
     * 设置负载均衡器
     * @param lb
     */
    void setLoadBalancer(ILoadBalancer lb);

    /**
     * 当请求发生异常时触发事件
     * @param event
     */
    void whenRequestFail(RequestExceptionFailEvent event);

    /**
     * 当请求发生异常时触发事件
     * @param event
     */
    void whenRequestSuccess(RequestSuccessEvent event);
}