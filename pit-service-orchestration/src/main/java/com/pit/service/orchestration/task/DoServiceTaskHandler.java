/**
 *
 */
package com.pit.service.orchestration.task;


import com.pit.core.exception.ExceptionUtils;
import com.pit.service.orchestration.*;
import com.pit.service.orchestration.bean.ResultVO;
import com.pit.service.orchestration.constants.BaseResultCodeConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gy
 *
 */
@Slf4j
public class DoServiceTaskHandler<I, R> extends AbstractTaskHandler<I, R> {


    public static final String TYPE = "service";

    public DoServiceTaskHandler(IService<I, R> service, IServiceLogService logService, IServiceBefore<I, R> serviceBefore) {
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
            result = exc(income, output, true);
        } catch (Throwable t) {
            log.error(this.getServiceInfo().getSimpleClassName() + "." + TYPE + " error occor:", t);
            // doservice的任务并且任务内部都是成功的才设置成框架的error
            output.setResultCode(this.getClass(), BaseResultCodeConstants.CODE_900000);
            output.addResultMsg("system error occur:" + ExceptionUtils.getStackTrace(t));
        } finally {
            output.setLatestServiceName(this.serviceInfo.getClassName());
        }
        return result;
    }

}
