package com.pit.service.orchestration;

import com.pit.service.orchestration.bean.ResultVO;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
public interface IService<I, R> {
    /**
     * 执行回调
     *
     * @param income
     * @param output
     * @return
     * @throws Exception
     */
    public AbstractCallback doService(I income, ResultVO<R> output) throws Exception;

    /**
     * 成功回调
     *
     * @param income
     * @param output
     * @return
     * @throws Exception
     */
    default AbstractCallback doSuccess(I income, ResultVO<R> output) throws Exception {
        return null;
    }

    /**
     * 失败回调
     *
     * @param income
     * @param output
     * @return
     * @throws Exception
     */
    default AbstractCallback doFail(I income, ResultVO<R> output) throws Exception {
        return null;
    }

    /**
     * 完成回调
     *
     * @param income
     * @param output
     * @return
     * @throws Exception
     */
    default AbstractCallback doComplate(I income, ResultVO<R> output) throws Exception {
        return null;
    }
}
