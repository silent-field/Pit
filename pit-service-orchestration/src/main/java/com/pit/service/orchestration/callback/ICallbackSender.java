package com.pit.service.orchestration.callback;

import com.pit.service.orchestration.AbstractCallback;

/**
 * @author gy
 */
public interface ICallbackSender<T extends AbstractCallback> {

    /**
     * 传递CallBack
     *
     * @param callback
     * @return true send成功，false send失败
     */
    boolean doSend(T callback);

}
