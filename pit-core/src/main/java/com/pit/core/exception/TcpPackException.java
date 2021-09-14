package com.pit.core.exception;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/12.
 */
public class TcpPackException extends CommonException {
	public TcpPackException(int code, String str) {
		super(code, str);
	}

	public TcpPackException(ResultCodes.ResultCode resultCode) {
		super(resultCode);
	}
}
