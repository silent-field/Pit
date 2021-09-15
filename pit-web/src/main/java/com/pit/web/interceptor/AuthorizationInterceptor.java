package com.pit.web.interceptor;

import com.pit.web.annotation.Auth;
import com.pit.web.exception.AuthenticationException;
import com.pit.web.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 鉴权拦截器
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
@Slf4j
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApplicationContext context = (ApplicationContext) request.getServletContext()
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        AuthenticationService authenticationService;
        try {
            authenticationService = context.getBean(AuthenticationService.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new AuthenticationException(-1, "Not found authenticationService impl!");
        }
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Auth auth = ((HandlerMethod) handler).getMethodAnnotation(Auth.class);
            if (null == auth) {
                auth = handlerMethod.getBeanType().getAnnotation(Auth.class);
            }
            if (null == auth || !auth.check()) {
                return true;
            }
            authenticationService.check(request);
        }
        return true;
    }
}