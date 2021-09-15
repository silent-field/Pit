package com.pit.web.annotation;

import java.lang.annotation.*;

/**
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Auth {

    /** 是否进行鉴权 */
    boolean check() default true;
}
