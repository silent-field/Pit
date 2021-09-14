package com.pit.http.async;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Response;

/**
 * Async http client callback
 *
 * @author gy
 * @date 2020/3/20
 */
public abstract class AsynHttpFutureCallback implements AsyncHandler<Response> {

    private final Response.ResponseBuilder builder = new Response.ResponseBuilder();
    private boolean isDone = false;
    private Response response;
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

    /**
     * 完成时调用
     *
     * @param response
     * @param t
     */
    protected abstract void onComplete(Response response, Throwable t);

    /**
     * 失败时调用
     *
     * @param t
     */
    protected abstract void onFail(Throwable t);

    /**
     * 成功时调用
     *
     * @param response
     */
    protected abstract void onSuccess(Response response);

}
