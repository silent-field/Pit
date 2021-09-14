package com.pit.loadbalance.meta;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pit.core.json.GsonUtils;
import com.pit.core.localcache.ICacheDataLoader;
import com.pit.core.localcache.guava.GuavaCacheConfig;
import com.pit.core.localcache.guava.PermanentCache;
import com.pit.core.text.StringUtils2;
import com.pit.loadbalance.config.Const;
import com.pit.loadbalance.config.LoadBalanceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 应用服务器信息持有者。持有服务器信息缓存，并定时更新
 *
 * @Author gy
 * @Date 2020-03-20 17:12
 */
@Slf4j
public abstract class ServiceMetaHolder implements IServiceMetaHolder {
    public static final Integer CLOSE_THREAD_POOL_WAIT_TIME = 3000;
    private PermanentCache<String, ServiceMetaInfo> serviceMetaInfoCache;
    private ExecutorService refreshPool = MoreExecutors.listeningDecorator(Executors
            .newFixedThreadPool(Const.REFRESH_POOL_SIZE,
                    new ThreadFactoryBuilder().setNameFormat("ServiceMetaRefresh").build()));

    public ServiceMetaHolder(LoadBalanceConfig config, CenterCacheLoadDataHandler centerCacheLoadDataHandler) {
        // serviceMetaInfoCache
        Integer serviceMetaRefreshTime = StringUtils.isNotBlank(config.getServiceMetaRefreshTime()) ?
                Integer.valueOf(config.getServiceMetaRefreshTime()) :
                Const.SERVICE_META_REFRESH_TIME;
        Integer serviceMetaRefreshMaxsize = StringUtils.isNotBlank(config.getServiceMetaMaxsize()) ?
                Integer.valueOf(config.getServiceMetaMaxsize()) :
                Const.SERVICE_META_MAXSIZE;

        GuavaCacheConfig guavaCacheConfig = GuavaCacheConfig.builder()
                .refreshDuration(serviceMetaRefreshTime).refreshTimeUnit(TimeUnit.SECONDS)
                .expireAfterWriteDuration(-1).maxSize(serviceMetaRefreshMaxsize)
                .build();

        serviceMetaInfoCache = new PermanentCache(guavaCacheConfig, refreshPool, centerCacheLoadDataHandler) {
            @Override
            protected String getName() {
                return "ServiceMetaHolder";
            }
        };
        // serviceMetaInfoCache end
        log.info(StringUtils2
                .format("initialize ServiceMetaHolder。refresh rate {} second，Maximum number of caches is {}",
                        serviceMetaRefreshTime, serviceMetaRefreshMaxsize));
    }

    @Override
    public void destroy() {
        MoreExecutors
                .shutdownAndAwaitTermination(refreshPool, CLOSE_THREAD_POOL_WAIT_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public ServiceMetaInfo getServiceMetaInfo(String symbol) {
        try {
            return serviceMetaInfoCache.get(symbol);
        } catch (Exception e) {
            log.error("exception occurs, ServiceMetaHolder.ServiceMetaInfoCache.getValue", e);
        }

        return new ServiceMetaInfo();
    }

    // ----------------------- Cache Load data handler，通过HTTP请求从Kong Agent查询同区集群信息
    public abstract class CenterCacheLoadDataHandler implements ICacheDataLoader<String, ServiceMetaInfo> {
        @Override
        public ServiceMetaInfo loadData(String symbol) {
            try {
                ServiceMetaInfo latest = getServicesFromCenter(symbol);
                log.info(StringUtils2
                        .format("query the latest cluster information，service symbol：{}，meta info：{}", symbol,
                                GsonUtils.toJson(latest)));
                return latest;
            } catch (Exception e) {
                log.error("Unable to get the latest cluster information from Kong Agent，service symbol：" + symbol, e);
            }

            return new ServiceMetaInfo();
        }

        protected abstract ServiceMetaInfo getServicesFromCenter(String symbol) throws Exception;
    }
}
