package com.andrew.common.loadbalance.meta;

import com.alibaba.fastjson.JSON;
import com.andrew.common.cache.guava.ICacheLoadDataHandler;
import com.andrew.common.cache.guava.PermanentCache;
import com.andrew.common.cache.guava.config.BaseCacheBuilderConfig;
import com.andrew.common.loadbalance.config.Const;
import com.andrew.common.loadbalance.config.LoadBalanceConfig;
import com.andrew.common.text.StringUtil;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 应用服务器信息持有者。持有服务器信息缓存，并定时更新
 *
 * @Author Andrew
 * @Date 2019-06-12 17:12
 */
@Slf4j
public abstract class ServiceMetaHolder implements IServiceMetaHolder {
	private PermanentCache<String, ServiceMetaInfo> serviceMetaInfoCache;

	public static final Integer CLOSE_THREAD_POOL_WAIT_TIME = 3000;

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

		serviceMetaInfoCache = new PermanentCache<>(
				new BaseCacheBuilderConfig(serviceMetaRefreshTime, TimeUnit.SECONDS, -1, null,
						serviceMetaRefreshMaxsize), refreshPool, centerCacheLoadDataHandler);
		// serviceMetaInfoCache end
		log.info(StringUtil
				.format("initialize ServiceMetaHolder。refresh rate {} second，Maximum number of caches is {}",
						serviceMetaRefreshTime, serviceMetaRefreshMaxsize));
	}

	// ----------------------- Cache Load data handler，通过HTTP请求从Kong Agent查询同区集群信息
	public abstract class CenterCacheLoadDataHandler implements ICacheLoadDataHandler<String, ServiceMetaInfo> {
		public ServiceMetaInfo loadData(String symbol) {
			try {
				ServiceMetaInfo latest = getServicesFromCenter(symbol);
				log.info(StringUtil
						.format("query the latest cluster information，service symbol：{}，meta info：{}", symbol,
								JSON.toJSONString(latest)));
				return latest;
			} catch (Exception e) {
				log.error("Unable to get the latest cluster information from Kong Agent，service symbol：" + symbol, e);
			}

			return new ServiceMetaInfo();
		}

		protected abstract ServiceMetaInfo getServicesFromCenter(String symbol) throws Exception;
	}

	@Override
	public void destroy() {
		MoreExecutors
				.shutdownAndAwaitTermination(refreshPool, CLOSE_THREAD_POOL_WAIT_TIME, TimeUnit.MILLISECONDS);
	}

	@Override
	public ServiceMetaInfo getServiceMetaInfo(String symbol) {
		try {
			return serviceMetaInfoCache.getValue(symbol);
		} catch (Exception e) {
			log.error("exception occurs, ServiceMetaHolder.ServiceMetaInfoCache.getValue", e);
		}

		return new ServiceMetaInfo();
	}
}
