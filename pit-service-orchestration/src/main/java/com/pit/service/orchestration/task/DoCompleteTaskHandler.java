package com.pit.service.orchestration.task;

import com.pit.core.exception.ExceptionUtils;
import com.pit.service.orchestration.*;
import com.pit.service.orchestration.bean.ResultVO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gy
 */
@Slf4j
public class DoCompleteTaskHandler<I, R> extends AbstractTaskHandler<I, R> {

    public static final String TYPE = "complete";

    public DoCompleteTaskHandler(IService<I, R> service, IServiceLogService logService, IServiceBefore<I, R> serviceBefore) {
        super(service, logService, serviceBefore, TYPE);
    }

    @Override
    public AbstractCallback doTask(I income, ResultVO<R> output) throws Exception {
        AbstractCallback result = null;
        try {
            output.setCurrentTaskHandler(this);
            if (null != this.serviceBefore && this.serviceBefore.dealServiceBefore(serviceInfo, income, output)) {
                return null;
            }
            result = exc(income, output, false);
        } catch (Throwable t) {
            log.error(this.getServiceInfo().getSimpleClassName() + "." + TYPE + " error occor:", t);
            output.addResultMsg("system error occur:" + ExceptionUtils.getStackTrace(t));
        }
        return result;
    }

}
