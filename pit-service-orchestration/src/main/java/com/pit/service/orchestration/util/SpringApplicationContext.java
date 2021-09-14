package com.pit.service.orchestration.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author gy
 */
@Component
public class SpringApplicationContext implements ApplicationContextAware {
    protected static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    @Override
    public void setApplicationContext(
            ApplicationContext applicationContext)
            throws BeansException {
        context = applicationContext;
    }
}
