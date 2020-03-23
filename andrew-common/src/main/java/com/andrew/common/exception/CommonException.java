package com.andrew.common.exception;

import lombok.Data;

/**
 * 通用异常
 *
 * @author Andrew
 * @date 2020/3/20
 */
@Data
public class CommonException extends RuntimeException {
	private String message;
	private int code;
	private String debugMsg;

	public CommonException(int code, String str) {
		super(str);
		this.code = code;
		this.message = str;
		this.debugMsg = str;
	}

	public CommonException(ResultCodes.ResultCode resultCode) {
		super(resultCode.getMessage());
		this.code = resultCode.getCode();
		this.message = resultCode.getMessage();
	}

	@Override
	public String toString() {
		return "CommonException{" +
				"message='" + message + '\'' +
				", code=" + code +
				", debugMsg='" + debugMsg + '\'' +
				'}';
	}
}
