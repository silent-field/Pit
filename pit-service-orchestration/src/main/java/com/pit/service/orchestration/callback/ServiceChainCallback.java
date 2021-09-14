package com.pit.service.orchestration.callback;

import com.pit.service.orchestration.AbstractCallback;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
@Slf4j
public class ServiceChainCallback extends AbstractCallback<Object, Throwable, Object, Object> {
    public ServiceChainCallback(long waitSecond) {
        super(Thread.currentThread().getId(), waitSecond);
    }

    @Override
    public void dealSuccess(Object result) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dealFail(Throwable e) {
        log.error("ServiceChainCallback fail:", e);
    }

    @Override
    public void dealComplete(Object result, Throwable e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dealException(Throwable t) {
        log.error("ServiceChainCallback" +
                " exception:", t);
    }
}
