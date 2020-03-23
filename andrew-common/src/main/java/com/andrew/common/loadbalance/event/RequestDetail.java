package com.andrew.common.loadbalance.event;

import com.alibaba.fastjson.JSONObject;
import com.andrew.common.loadbalance.ILoadBalancer;
import com.andrew.common.loadbalance.meta.ServiceMetaInfo;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class RequestDetail {
    private LoadBalanceRequestWrapper request;

	private String currentHost;

	private String serviceSymbol;

	private ServiceMetaInfo.NodeMetaInfo nodeMetaInfo;

	private String url;

	private String jsonBody;

	private boolean businessAlarmSwitch;

	private long slowThreshold;

	private ILoadBalancer loadBalancer;

	@Data
	public static class LoadBalanceRequestWrapper {
		/**
		 * 请求路径
		 */
		private String path;

		/**
		 * 请求参数集
		 */
		private JSONObject params;

		/**
		 * Http Header
		 */
		private Map<String, String> headers;
	}
}