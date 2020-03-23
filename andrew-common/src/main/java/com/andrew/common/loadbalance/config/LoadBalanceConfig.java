package com.andrew.common.loadbalance.config;

import lombok.Data;

/**
 * 负载均衡配置项
 *
 * @Author Andrew
 * @Date 2019-06-13 14:56
 */
@Data
public class LoadBalanceConfig {
	private String serviceMetaRefreshTime;

	private String serviceMetaMaxsize;
}
