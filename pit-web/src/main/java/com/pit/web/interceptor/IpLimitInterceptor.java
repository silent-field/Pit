package com.pit.web.interceptor;

import com.pit.core.util.EnvUtil;
import com.pit.web.exception.AuthenticationException;
import com.pit.web.exception.IpLimitException;
import com.pit.web.service.WebListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * ip黑名单拦截器
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
@Slf4j
public class IpLimitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            doCheck(request);
        }
        return true;
    }

    private void doCheck(HttpServletRequest request) throws IpLimitException {
        ApplicationContext context = (ApplicationContext) request.getServletContext()
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        WebListService webListService;
        try {
            webListService = context.getBean(WebListService.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new AuthenticationException(-1, "Not found WebListService impl!");
        }

        Set<String> ipBlackSet = webListService.ipBlackList();
        String requestIp = request.getRemoteAddr();
        if (!EnvUtil.isDevEnv()) {
            if (ipBlackSet.contains(requestIp)) {
                log.error("request ip invalid,requestIp:" + requestIp);
                throw new IpLimitException(12088, "request ip invalid,requestIp:" + requestIp);
            }
        }

    }
}
