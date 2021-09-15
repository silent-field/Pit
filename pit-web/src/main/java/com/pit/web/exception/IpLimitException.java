package com.pit.web.exception;

import com.pit.core.exception.CommonException;

/**
 * ip限制异常
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
public class IpLimitException extends CommonException {

    public IpLimitException(int code, String msg) {
        super(code, msg);
    }
}
