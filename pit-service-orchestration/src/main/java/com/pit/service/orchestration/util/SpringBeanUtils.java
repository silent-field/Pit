package com.pit.service.orchestration.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
@Component
@Slf4j
public class SpringBeanUtils {
    public static String getRootRealPath() {
        String rootRealPath = "";
        try {
            rootRealPath = SpringApplicationContext.getContext().getResource("").getFile().getAbsolutePath();
        } catch (IOException e) {
            log.warn("获取系统根目录失败");
        }
        return rootRealPath;
    }

    public static String getResourceRootRealPath() {
        String rootRealPath = "";
        try {
            rootRealPath = new DefaultResourceLoader().getResource("").getFile().getAbsolutePath();
        } catch (IOException e) {
            log.warn("获取资源根目录失败");
        }
        return rootRealPath;
    }

    /**
     * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) SpringApplicationContext.getContext().getBean(name);
    }

    /**
     * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    public static <T> T getBean(Class<T> requiredType) {
        return SpringApplicationContext.getContext().getBean(requiredType);
    }

    /**
     * 添加一个bean
     *
     * @param clazz
     * @param serviceName
     * @param propertyMap
     */
    public static void addBean(Class<?> clazz, String serviceName, Map<?, ?> propertyMap) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        if (propertyMap != null) {
            Iterator<?> entries = propertyMap.entrySet().iterator();
            Map.Entry<?, ?> entry;
            while (entries.hasNext()) {
                entry = (Map.Entry<?, ?>) entries.next();
                String key = (String) entry.getKey();
                Object val = entry.getValue();
                beanDefinitionBuilder.addPropertyValue(key, val);
            }
        }
        registerBean(serviceName, beanDefinitionBuilder.getRawBeanDefinition());
    }

    /**
     * @param beanName
     * @param beanDefinition
     * @desc 向spring容器注册bean
     */
    private static void registerBean(String beanName, BeanDefinition beanDefinition) {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringApplicationContext.getContext();
        BeanDefinitionRegistry beanDefinitonRegistry = (BeanDefinitionRegistry) configurableApplicationContext.getBeanFactory();
        beanDefinitonRegistry.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * getSpringBean 获取SpringBean
     *
     * @param <T> type
     * @param t   t
     * @return <T> type
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(T t) {
        if (SpringApplicationContext.getContext().containsBean(t.getClass().getSimpleName())) {
            return (T) SpringApplicationContext.getContext().getBean(t.getClass().getSimpleName());
        }
        return (T) SpringApplicationContext.getContext().getBean(getBeanName(t.getClass().getSimpleName()));
    }

    /**
     * getSpringBean 获取SpringBean
     *
     * @param <T>   type
     * @param clazz class
     * @param name  name
     * @return <T> type
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz, String name) {
        return (T) SpringApplicationContext.getContext().getBean(name);
    }

    /**
     * getSpringBeansByType 根据类型获取SpringBean
     *
     * @param clazz 类
     * @return String[] String[]
     */
    public static String[] getSpringBeanNamesByType(Class<?> clazz) {
        return SpringApplicationContext.getContext().getBeanNamesForType(clazz);
    }

    /**
     * getSpringBeansByType 根据类型获取SpringBean
     *
     * @param clazz 类
     * @return String[] String[]
     */
    public static <T> T getSpringBeanByType(Class<?> clazz) {
        String[] names = SpringApplicationContext.getContext().getBeanNamesForType(clazz);
        if (null == names || names.length == 0) {
            return null;
        }
        if (StringUtils.isBlank(names[0])) {
            return null;
        }

        return (T) SpringApplicationContext.getContext().getBean(names[0]);
    }

    /**
     * getBeanName 获取Bean名字
     *
     * @param className 类名
     * @return String
     */
    public static String getBeanName(String className) {

        String firstChar = className.substring(0, 1);
        return firstChar.toLowerCase() + className.substring(1);
    }
}
