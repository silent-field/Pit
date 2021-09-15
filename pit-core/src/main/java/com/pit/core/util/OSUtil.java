package com.pit.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 操作系统相关工具类
 * @Author: gy
 * @Date: 2021/9/15
 */
public class OSUtil {
    /**
     * 判断是否为Windows操作系统
     *
     * @return {@code true} 表示为Windows操作系统
     */
    public static boolean isWindows() {
        String osName = System.getProperty("os.name");
        if (StringUtils.isBlank(osName)) {
            return false;
        }
        return osName.startsWith("Windows");
    }

    /**
     * 判断是否为Mac OS系统
     *
     * @return {@code true} 表示为MacOS系统
     */
    public static boolean isMacOS() {
        String osName = System.getProperty("os.name");
        if (StringUtils.isBlank(osName)) {
            return false;
        }
        return osName.startsWith("Mac OS");
    }
}
