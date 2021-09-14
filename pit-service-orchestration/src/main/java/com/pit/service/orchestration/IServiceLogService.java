package com.pit.service.orchestration;

import com.pit.core.log.ILogService;
import com.pit.service.orchestration.bean.ResultVO;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
public interface IServiceLogService extends ILogService {
    /**
     * 打印running日志
     *
     * @param income
     * @param output
     * @param methodName
     * @param remark
     * @param beginTime
     */
    default void sendRunningAccountLog(Object income, ResultVO output, String methodName, String remark, Long beginTime) {
        return;
    }
}
