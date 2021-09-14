package com.pit.loadbalance.config;

import lombok.Data;

/**
 * 负载均衡配置项
 *
 * @Author gy
 * @Date 2020-03-20 14:56
 */
@Data
public class LoadBalanceConfig {
	private String serviceMetaRefreshTime;

	private String serviceMetaMaxsize;
}
