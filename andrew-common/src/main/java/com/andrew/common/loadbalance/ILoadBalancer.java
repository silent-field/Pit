package com.andrew.common.loadbalance;

import com.andrew.common.loadbalance.event.RequestExceptionFailEvent;
import com.andrew.common.loadbalance.event.RequestSuccessEvent;
import com.andrew.common.loadbalance.meta.ServiceMetaInfo;

import java.util.Comparator;

/**
 * 负载均衡器接口
 *
 * @Author Andrew
 * @Date 2019-06-14 15:09
 */
public interface ILoadBalancer {
	/**
	 * 设置服务集群
	 *
	 * @param serviceMetaInfo
	 */
	void setServers(ServiceMetaInfo serviceMetaInfo);

	/**
	 * 通过负载均衡器选出一个节点
	 *
	 * @return
	 */
	ServiceMetaInfo.NodeMetaInfo chooseNode(Object key);

	/**
	 * 获取服务集群
	 *
	 * @return
	 */
	ServiceMetaInfo getServiceMetaInfo();

	/**
	 * 用于对{@linkplain ServiceMetaInfo.NodeMetaInfo}排序
	 *
	 * @return
	 */
	Comparator<ServiceMetaInfo.NodeMetaInfo> getSortComparator();

	/**
	 * 当请求发生异常时触发事件
	 *
	 * @param event
	 */
	void fireRequestException(RequestExceptionFailEvent event);

	/**
	 * 当请求发生成功时触发事件
	 *
	 * @param event
	 */
	void fireRequestSuccess(RequestSuccessEvent event);
}
