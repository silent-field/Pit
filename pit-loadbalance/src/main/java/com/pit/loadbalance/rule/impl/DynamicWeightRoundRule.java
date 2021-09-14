package com.pit.loadbalance.rule.impl;

import com.pit.loadbalance.ILoadBalancer;
import com.pit.loadbalance.event.RequestExceptionFailEvent;
import com.pit.loadbalance.event.RequestSuccessEvent;
import com.pit.loadbalance.meta.ServiceMetaInfo;
import com.pit.loadbalance.rule.AbstractLoadBalancerRule;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author gy
 * @Date 2020-03-20 16:57
 * @Description 动态加权路由策略
 * 假设有一组服务器S = {S0, S1, …, Sn-1}，W(Si)表示服务器Si的权值，
 * 一个指示变量i表示上一次选择的服务器，指示变量cw表示当前调度的权值，m
 * ax(S)表示集合S中所有服务器的最大权值，gcd(S)表示集合S中所有服务器权值的最大公约数。
 * 变量i初始化为-1，cw初始化为零。
 * <p>
 * 由于存在共享变量，在高并发的情况下不会非常精确
 */
public class DynamicWeightRoundRule extends AbstractLoadBalancerRule {
    /**
     * 上次选择的服务器
     */
    private int currentIndex;
    /**
     * 当前调度的权值
     */
    private int currentWeight;
    /**
     * 最大权重
     */
    private int maxWeight;
    /**
     * 权重的最大公约数
     */
    private int gcdWeight;
    /**
     * 服务器数
     */
    private int serverCount;

    private Object refreshLock = new Object();

    // --------- 动态调整权重
    private Integer initWeight = 100;
    private Integer failLimit = 10;
    private Integer period = 1000;
    private Integer degrade = 10;

    //
    private Integer successLimit = 10;
    private Integer upgrade = 10;

    private static List<ServerWeight> serverWeights = new ArrayList<>();
    private static Map<String, FailCountLimit> failCountLimitMap = new HashMap<>();
    private static Map<String, SuccessCountLimit> successCountLimitMap = new HashMap<>();

    public DynamicWeightRoundRule(ILoadBalancer loadBalancer) {
        super(loadBalancer);
        initWeight();
    }

    public DynamicWeightRoundRule(ILoadBalancer loadBalancer, Integer initWeight, Integer failLimit, Integer period,
                                  Integer degrade, Integer successLimit, Integer upgrade) {
        super(loadBalancer);
        initWeight();

        this.initWeight = initWeight;
        this.failLimit = failLimit;
        this.period = period;
        this.degrade = degrade;

        this.successLimit = successLimit;
        this.upgrade = upgrade;
    }

    private void initWeight() {
        if (getLoadBalancer().getServiceMetaInfo() != null) {
            if (CollectionUtils.isNotEmpty(getLoadBalancer().getServiceMetaInfo().getClusterNetworkMetaInfo())) {
                for (ServiceMetaInfo.NodeMetaInfo nodeMetaInfo : getLoadBalancer().getServiceMetaInfo()
                        .getClusterNetworkMetaInfo()) {
                    serverWeights.add(new ServerWeight(nodeMetaInfo.getTarget(), initWeight));
                    failCountLimitMap.put(nodeMetaInfo.getTarget(),
                            new FailCountLimit(failLimit, period, TimeUnit.MILLISECONDS));
                }
            }
        }
        updateWeight();
    }

    private void updateWeight() {
        currentIndex = -1;
        currentWeight = 0;
        serverCount = serverWeights.size();
        maxWeight = greatestWeight();
        gcdWeight = greatestCommonDivisor();
    }

    /*
     * 得到两值的最大公约数
     */
    public int greaterCommonDivisor(int a, int b) {
        if (a % b == 0) {
            return b;
        } else {
            return greaterCommonDivisor(b, a % b);
        }
    }

    /*
     * 得到list中所有权重的最大公约数，实际上是两两取最大公约数d，然后得到的d
     * 与下一个权重取最大公约数，直至遍历完
     */
    public int greatestCommonDivisor() {
        int divisor = 0;
        for (int index = 0; index < serverWeights.size(); index++) {
            if (index == 0) {
                divisor = greaterCommonDivisor(serverWeights.get(index).getWeight(),
                        serverWeights.get(index + 1).getWeight());
            } else {
                divisor = greaterCommonDivisor(divisor, serverWeights.get(index).getWeight());
            }
        }
        return divisor;
    }

    /*
     * 得到list中的最大的权重
     */
    public int greatestWeight() {
        int weight = 0;
        for (ServerWeight serverWeight : serverWeights) {
            if (weight < serverWeight.weight) {
                weight = serverWeight.weight;
            }
        }
        return weight;
    }

    @Override
    public ServiceMetaInfo.NodeMetaInfo choose(ILoadBalancer lb, Object key) {
        List<ServiceMetaInfo.NodeMetaInfo> clusterNetworkMetaInfo = lb.getServiceMetaInfo().getClusterNetworkMetaInfo();
        while (true) {
            currentIndex = (currentIndex + 1) % serverCount;
            if (currentIndex == 0) {
                currentWeight = currentWeight - gcdWeight;
                if (currentWeight <= 0) {
                    currentWeight = maxWeight;
                    if (currentWeight == 0) {
                        return null;
                    }
                }
            }
            if (serverWeights.get(currentIndex).getWeight() >= currentWeight) {
                String target = serverWeights.get(currentIndex).getTarget();

                ServiceMetaInfo.NodeMetaInfo selected = null;
                for (ServiceMetaInfo.NodeMetaInfo nodeMetaInfo : clusterNetworkMetaInfo) {
                    if (target.equalsIgnoreCase(nodeMetaInfo.getTarget())) {
                        selected = nodeMetaInfo;
                    }
                }

                return selected;
            }
        }
    }

    @Override
    public void whenRequestFail(RequestExceptionFailEvent event) {
        String target = event.getTarget().getNodeMetaInfo().getTarget();

        if (failCountLimitMap.containsKey(target)) {
            Pair<Boolean, Boolean> pair = failCountLimitMap.get(target).grant();

            if (!pair.getLeft() && pair.getRight()) {
                synchronized (refreshLock) {
                    ServerWeight expect = null;
                    for (ServerWeight serverWeight : serverWeights) {
                        if (serverWeight.getTarget().equalsIgnoreCase(target)) {
                            expect = serverWeight;
                            break;
                        }
                    }

                    if (expect != null) {
                        Integer toUpdate = expect.getWeight() - degrade;
                        if (toUpdate <= 0) {
                            // serverWeights.remove(expect);
                            // 给机会重新加权
                            expect.setWeight(1);

                            if (!successCountLimitMap.containsKey(target)) {
                                successCountLimitMap.put(target, new SuccessCountLimit(successLimit));
                            }
                        } else {
                            expect.setWeight(toUpdate);
                        }

                        updateWeight();
                    }
                }
            }
        }
    }

    @Override
    public void whenRequestSuccess(RequestSuccessEvent event) {
        String target = event.getTarget().getNodeMetaInfo().getTarget();

        if (successCountLimitMap.containsKey(target)) {
            boolean grant = successCountLimitMap.get(target).grant();

            if (!grant) {
                synchronized (refreshLock) {
                    ServerWeight expect = null;
                    for (ServerWeight serverWeight : serverWeights) {
                        if (serverWeight.getTarget().equalsIgnoreCase(target)) {
                            expect = serverWeight;
                            break;
                        }
                    }

                    if (expect != null) {
                        Integer toUpdate = expect.getWeight() + upgrade;
                        if (toUpdate >= initWeight) {
                            // 恢复到原始权重
                            expect.setWeight(initWeight);
                            successCountLimitMap.remove(target);
                        } else {
                            expect.setWeight(toUpdate);
                        }

                        updateWeight();
                    }
                }
            }
        }
    }

    @Data
    @Builder
    public static class ServerWeight {
        private String target;
        private Integer weight;
    }

    // --------------------------------------

    /**
     * 错误次数统计
     */
    public static class FailCountLimit {
        private long startPoint;

        private int count = 0;

        /**
         * 上限
         */
        private int limit;

        /**
         * 时间间隔
         */
        private long period;

        private boolean overNotify;

        private final Object lock = new Object();

        /**
         * @param limit    限制次数
         * @param period   时间间隔
         * @param timeUnit 间隔类型
         */
        public FailCountLimit(int limit, int period, TimeUnit timeUnit) {
            this.startPoint = System.currentTimeMillis();
            this.period = timeUnit.toMillis(period);
            this.limit = limit;
            overNotify = false;
        }


        public Pair<Boolean, Boolean> grant() {
            long curTime = System.currentTimeMillis();
            synchronized (lock) {
                count++;
                if (count > limit) {
                    if (curTime - startPoint > period) {
                        startPoint = curTime;
                        count = 0;
                        overNotify = false;
                        return ImmutablePair.of(true, null);
                    } else {
                        if (!overNotify) {
                            overNotify = true;
                            return ImmutablePair.of(false, true);
                        }
                        return ImmutablePair.of(false, false);
                    }
                } else {
                    return ImmutablePair.of(true, null);
                }
            }
        }
    }

    // ---------------------

    /**
     * 成功次数统计
     */
    public static class SuccessCountLimit {
        private int count = 0;

        /**
         * 上限
         */
        private int limit;

        private final Object lock = new Object();

        /**
         * @param limit 限制次数
         */
        public SuccessCountLimit(int limit) {
            this.limit = limit;
        }


        public boolean grant() {
            synchronized (lock) {
                count++;
                if (count > limit) {
                    // reset
                    count = 0;
                    return false;
                } else {
                    return true;
                }
            }
        }
    }
}