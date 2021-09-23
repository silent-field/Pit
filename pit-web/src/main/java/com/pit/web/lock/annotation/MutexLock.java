package com.pit.web.lock.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MutexLock {

    /**
     * 参数 el表达式
     *
     * @return
     */
    String[] parameters() default {};

    /**
     * 持锁时间，单位秒
     *
     * @return
     */
    int leaseTime() default 10;

    /**
     * 是否自动解锁，如果为true，请求处理完解锁，否则处理时长超过leaseTime后自动解锁
     *
     * @return
     */
    boolean autoUnlock() default true;
}