package com.andrew.common.loadbalance.event;

import lombok.Data;

/**
 * @Description
 * @Author Andrew
 * @Date 2019-08-02 14:52
 */
@Data
public class RequestExceptionFailEvent {
	private RequestDetail target;

	private Exception failException;

	public RequestExceptionFailEvent(RequestDetail target, Exception failException) {
		this.target = target;
		this.failException = failException;
	}
}
