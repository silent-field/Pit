/**
 *
 */
package com.pit.service.orchestration.task;

import com.pit.service.orchestration.AbstractCallback;
import com.pit.service.orchestration.AbstractTaskHandler;
import com.pit.service.orchestration.IServiceLogService;
import com.pit.service.orchestration.bean.ResultVO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gy
 *
 */
@Slf4j
public class DoFinalTaskHandler extends AbstractTaskHandler<Object, Object> {

    public static final String TYPE = "final";

    public DoFinalTaskHandler(IServiceLogService logService) {
        super(null, logService, null, TYPE);
    }

    @Override
    public AbstractCallback doTask(Object income, ResultVO<Object> output) throws Exception {
        if (null != output.getScc()) {
            output.getScc().onSuccess(null);
        }
        return null;
    }

}
