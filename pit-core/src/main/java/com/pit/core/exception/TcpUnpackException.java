package com.pit.core.exception;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/12.
 */
public class TcpUnpackException extends CommonException {
    public TcpUnpackException(int code, String str) {
        super(code, str);
    }

    public TcpUnpackException(ResultCodes.ResultCode resultCode) {
        super(resultCode);
    }
}
