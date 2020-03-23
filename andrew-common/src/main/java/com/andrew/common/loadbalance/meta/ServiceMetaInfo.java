package com.andrew.common.loadbalance.meta;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用(集群)基本信息
 *
 * @Author Andrew
 * @Date 2019-06-12 17:14
 */
@Data
public class ServiceMetaInfo {
	// 应用的代号(唯一)
	String symbol;

	// 集群中每个Node的网络基本信息
	List<NodeMetaInfo> clusterNetworkMetaInfo = new ArrayList<>();

	public void addNodeNetworkMetaInfo(NodeMetaInfo node) {
		clusterNetworkMetaInfo.add(node);
	}

	/**
	 * 应用(单Node)的网络基本信息
	 */
	@Data
	public static class NodeMetaInfo {
		private String target;

		private String host;
		private int port;
		private int regionId;
		private int groupId;
		private String data;

		public NodeMetaInfo(String host, int port, int regionId, int groupId, String data) {
			this.host = host;
			this.port = port;
			this.regionId = regionId;
			this.groupId = groupId;
			this.data = data;
			this.target = host + ":" + port;
		}
	}
}