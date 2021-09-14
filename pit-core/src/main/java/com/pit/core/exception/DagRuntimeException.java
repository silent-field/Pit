package com.pit.core.exception;

/**
 * @author gy
 * @version 1.0
 * @date 2021/3/17.
 * @description:
 */
public class DagRuntimeException extends CommonException {
    public DagRuntimeException(int code, String str) {
        super(code, str);
    }

    public DagRuntimeException(ResultCodes.ResultCode resultCode) {
        super(resultCode);
    }
}
