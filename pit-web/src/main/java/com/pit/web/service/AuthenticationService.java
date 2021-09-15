package com.pit.web.service;


import com.pit.web.exception.AuthenticationException;

import javax.servlet.http.HttpServletRequest;

/**
 * 用于请求鉴权
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
public interface AuthenticationService {

    /**
     *
     * @param request
     * @throws AuthenticationException
     */
    void check(HttpServletRequest request) throws AuthenticationException;
}
