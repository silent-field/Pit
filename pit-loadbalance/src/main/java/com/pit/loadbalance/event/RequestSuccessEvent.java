package com.pit.loadbalance.event;

import lombok.Data;

/**
 * @Description
 * @Author gy
 * @Date 2019-08-02 14:52
 */
@Data
public class RequestSuccessEvent {
    private RequestDetail target;

    public RequestSuccessEvent(RequestDetail target) {
        this.target = target;
    }
}
