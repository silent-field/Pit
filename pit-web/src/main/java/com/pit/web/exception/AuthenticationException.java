package com.pit.web.exception;


import com.pit.core.exception.CommonException;

/**
 * 验证异常
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
public class AuthenticationException extends CommonException {

    public AuthenticationException(int code, String msg) {
        super(code, msg);
    }
}
