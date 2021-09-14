package com.pit.loadbalance.meta;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用(集群)元信息
 *
 * @Author gy
 * @Date 2020-03-20 17:14
 */
@Data
public class ServiceMetaInfo {
    /**
     * 应用唯一标识
     */
    private String symbol;

    /**
     * 集群中每个Node的网络基本信息
     */
    private List<NodeMetaInfo> clusterNetworkMetaInfo = new ArrayList<>();

    /**
     * 增加一个节点（实例）
     * @param node
     */
    public void addNodeNetworkMetaInfo(NodeMetaInfo node) {
        clusterNetworkMetaInfo.add(node);
    }

    /**
     * 应用节点的网络基本信息
     */
    @Data
    public static class NodeMetaInfo {
        private String target;

        /**
         * ip
         */
        private String host;
        /**
         * 端口
         */
        private int port;
        /**
         * 大区ID
         */
        private int regionId;
        /**
         * 机房ID
         */
        private int groupId;
        /**
         * 备注信息
         */
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