package com.pit.loadbalance.meta;

/**
 * @Description
 *
 * @Author gy
 * @Date 2020-03-20 15:14
 */
public class EmptyServiceMetaInfoHolder implements IServiceMetaHolder {
    @Override
    public ServiceMetaInfo getServiceMetaInfo(String symbol) {
        return new ServiceMetaInfo();
    }

    @Override
    public void destroy() {

    }
}
