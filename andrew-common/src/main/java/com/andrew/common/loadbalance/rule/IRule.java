package com.andrew.common.loadbalance.rule;

import com.andrew.common.loadbalance.ILoadBalancer;
import com.andrew.common.loadbalance.event.RequestExceptionFailEvent;
import com.andrew.common.loadbalance.event.RequestSuccessEvent;
import com.andrew.common.loadbalance.meta.ServiceMetaInfo;

/**
 * 负载均衡策略类
 *
 * @Author Andrew
 * @Date 2019-06-14 15:25
 */
public interface IRule {
	/**
	 * 通过负载均衡策略选出一个节点
	 * @return
	 */
	ServiceMetaInfo.NodeMetaInfo choose(Object key);

	/**
	 * 设置负载均衡器
	 * @param lb
	 */
	void setLoadBalancer(ILoadBalancer lb);

	/**
	 * 获取负载均衡器
	 * @return
	 */
	ILoadBalancer getLoadBalancer();

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