package com.pit.service.orchestration.bean;

import com.pit.service.orchestration.IService;
import com.pit.service.orchestration.annotation.ServiceErrorCode;
import com.pit.service.orchestration.annotation.ServiceOrder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
@Data
public class ServiceInfo<I, R> {
    private IService<I, R> service;

    private String simpleClassName;

    private String className;

    private int order;

    private String errorCode = "";

    public ServiceInfo(IService service) {
        super();
        this.service = service;
        Class clazz = AopUtils.getTargetClass(service);
        simpleClassName = clazz.getSimpleName();
        className = clazz.getName();
        try {
            ServiceOrder order = (ServiceOrder) clazz.getAnnotation(ServiceOrder.class);
            this.order = order.value();
            ServiceErrorCode errorCode = (ServiceErrorCode) clazz.getAnnotation(ServiceErrorCode.class);
            if (null != errorCode && StringUtils.isNotBlank(errorCode.value())) {
                this.errorCode = errorCode.value();
            }
        } catch (Exception e) {
            throw new RuntimeException(className + " error:", e);
        }
    }
}
