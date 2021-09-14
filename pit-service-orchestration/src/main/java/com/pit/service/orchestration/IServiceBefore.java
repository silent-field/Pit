package com.pit.service.orchestration;

import com.pit.service.orchestration.bean.ResultVO;
import com.pit.service.orchestration.bean.ServiceInfo;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
public interface IServiceBefore<I, R> {
    /**
     * 服务调用 doService 前执行
     *
     * @param serviceInfo
     * @param income
     * @param output
     * @return
     */
    boolean dealServiceBefore(ServiceInfo<I, R> serviceInfo, I income, ResultVO<R> output);

    /**
     * 服务成功调用 doSuccess 前执行
     *
     * @param serviceInfo
     * @param income
     * @param output
     * @return
     */
    boolean dealSuccessBefore(ServiceInfo<I, R> serviceInfo, I income, ResultVO<R> output);

    /**
     * 服务失败调用 doFail 前执行
     *
     * @param serviceInfo
     * @param income
     * @param output
     * @return
     */
    boolean dealFailBefore(ServiceInfo<I, R> serviceInfo, I income, ResultVO<R> output);

    /**
     * 服务完成调用 doComplete 前执行
     *
     * @param serviceInfo
     * @param income
     * @param output
     * @return
     */
    boolean dealCompleteBefore(ServiceInfo<I, R> serviceInfo, I income, ResultVO<R> output);
}
