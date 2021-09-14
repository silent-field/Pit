package com.pit.ext.system;

import com.pit.core.data.DataSizeUtil;
import com.pit.core.log.ILogService;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/17.
 * @description:
 */
public class RuntimeInfo {
    /**
     * 获得JVM最大内存
     *
     * @return 最大内存
     */
    public final static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 获得JVM已分配内存
     *
     * @return 已分配内存
     */
    public final static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * 获得JVM已分配内存中的剩余空间
     *
     * @return 已分配内存中的剩余空间
     */
    public final static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * 获得JVM最大可用内存
     *
     * @return 最大可用内存
     */
    public final static long getUsableMemory() {
        return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
    }

    public final static String printRuntimeInfo() {
        StringBuilder builder = new StringBuilder(128);

        ILogService.append(builder, "Max Memory: ", DataSizeUtil.prettyLook(getMaxMemory()), "\n");
        ILogService.append(builder, "Total Memory: ", DataSizeUtil.prettyLook(getTotalMemory()), "\n");
        ILogService.append(builder, "Free Memory: ", DataSizeUtil.prettyLook(getFreeMemory()), "\n");
        ILogService.append(builder, "Usable Memory: ", DataSizeUtil.prettyLook(getUsableMemory()), "\n");

        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.println(printRuntimeInfo());
    }
}
