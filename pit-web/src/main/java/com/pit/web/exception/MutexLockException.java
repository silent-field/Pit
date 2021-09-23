package com.pit.web.exception;

import com.pit.core.exception.CommonException;

public class MutexLockException extends CommonException {
	public MutexLockException(int code, String msg) {
		super(code, msg);
	}
}