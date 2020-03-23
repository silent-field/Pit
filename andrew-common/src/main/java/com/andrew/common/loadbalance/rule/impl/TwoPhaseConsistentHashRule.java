package com.andrew.common.loadbalance.rule.impl;

import com.alibaba.fastjson.JSON;
import com.andrew.common.loadbalance.ILoadBalancer;
import com.andrew.common.loadbalance.meta.ServiceMetaInfo;
import com.andrew.common.loadbalance.rule.AbstractLoadBalancerRule;
import com.andrew.common.text.StringUtil;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 两阶段一致性Hash策略
 *
 * @Author Andrew
 * @Date 2019-06-14 15:51
 */
@Slf4j
public class TwoPhaseConsistentHashRule extends AbstractLoadBalancerRule {
	public TwoPhaseConsistentHashRule(ILoadBalancer loadBalancer) {
		super(loadBalancer);
	}

	@Override
	public ServiceMetaInfo.NodeMetaInfo choose(ILoadBalancer lb, Object key) {
		/**
		 * 1. 对机房进行一致性hash选择机房
		 * 2. 对选择的机房内应用实例进行一致性hash选择应用实例
		 */
		List<ServiceMetaInfo.NodeMetaInfo> clusterNetworkMetaInfo = lb.getServiceMetaInfo().getClusterNetworkMetaInfo();

		// 根据机房groupId分组
		Pair<List<Integer>, Map<Integer, List<ServiceMetaInfo.NodeMetaInfo>>> groupByServerRoomPair = groupByServerRoom(
				clusterNetworkMetaInfo);

		if (groupByServerRoomPair == null || CollectionUtils.isEmpty(groupByServerRoomPair.getLeft()) || MapUtils
				.isEmpty(groupByServerRoomPair.getRight())) {
			log.warn("groupByServerRoom result is not available, ServiceMetaInfo is : " + JSON
					.toJSONString(lb.getServiceMetaInfo()));
			return null;
		}

		int hashcode = key.hashCode();

		// 对机房一致性hash
		int serverRoomCount = groupByServerRoomPair.getLeft().size();
		int selectedServerRoomIndex = Hashing.consistentHash(hashcode, serverRoomCount); // 使用Guava的一致性哈希算法
		int serverRoomId = groupByServerRoomPair.getLeft().get(selectedServerRoomIndex);

		// 对机房内应用实例一致性hash
		List<ServiceMetaInfo.NodeMetaInfo> nodeMetaInfoList = groupByServerRoomPair.getRight().get(serverRoomId);
		int nodeCount = nodeMetaInfoList.size();
		int selectedNodeIndex = Hashing.consistentHash(hashcode, nodeCount); // 使用Guava的一致性哈希算法

		return nodeMetaInfoList.get(selectedNodeIndex);
	}

	private Pair<List<Integer>, Map<Integer, List<ServiceMetaInfo.NodeMetaInfo>>> groupByServerRoom(
			List<ServiceMetaInfo.NodeMetaInfo> clusterNetworkMetaInfo) {
		// 使用List保证顺序跟kong agent返回顺序一致
		List<Integer> serverRoomList = new ArrayList<>();
		Map<Integer, List<ServiceMetaInfo.NodeMetaInfo>> groupByServerRoom = new HashMap<>();

		for (ServiceMetaInfo.NodeMetaInfo nodeMetaInfo : clusterNetworkMetaInfo) {
			if (StringUtils.isAnyBlank(nodeMetaInfo.getHost()) || nodeMetaInfo.getPort() == 0 || nodeMetaInfo.getGroupId() == 0) {
				log.error(StringUtil.format("ServiceMetaInfo.NodeMetaInfo:{} has no meta or groupId",
						JSON.toJSONString(nodeMetaInfo)));
				continue;
			}

			int groupId = nodeMetaInfo.getGroupId();
			if (!serverRoomList.contains(groupId)) {
				serverRoomList.add(groupId);
			}

			if (!groupByServerRoom.containsKey(groupId)) {
				groupByServerRoom.put(groupId, new ArrayList<>());
			}

			groupByServerRoom.get(groupId).add(nodeMetaInfo);
		}

		return ImmutablePair.of(serverRoomList, groupByServerRoom);
	}
}