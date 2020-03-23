package com.andrew.common.http.async;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Response;

/**
 * TODO 待添加上下文信息
 *
 * @author Andrew
 * @date 2020/3/20
 */
public abstract class AsynHttpFutureCallback implements AsyncHandler<Response> {

	private boolean isDone = false;

	private Response response;

	private final Response.ResponseBuilder builder = new Response.ResponseBuilder();

	private Throwable t;

	public AsynHttpFutureCallback() {
	}

	@Override
	public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
		builder.accumulate(responseStatus);
		return State.CONTINUE;
	}

	@Override
	public State onHeadersReceived(HttpHeaders headers) throws Exception {
		builder.accumulate(headers);
		return State.CONTINUE;
	}

	@Override
	public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
		builder.accumulate(bodyPart);
		return State.CONTINUE;
	}

	@Override
	public Response onCompleted() throws Exception {
		if (isDone) {
			return this.response;
		}
		this.response = builder.build();

		try {
			if (null != response) {
				onSuccess(response);
			} else {
				onFail(t);
			}
		} finally {
			onComplete(response, t);
			isDone = true;
		}
		return response;
	}

	@Override
	public void onThrowable(Throwable t) {
		if (isDone) {
			return;
		}
		try {
			onFail(t);
		} finally {
			onComplete(response, null);
			isDone = true;
		}
		this.t = t;
	}

	protected abstract void onComplete(Response response, Throwable t);

	protected abstract void onFail(Throwable t);

	protected abstract void onSuccess(Response response);

}
