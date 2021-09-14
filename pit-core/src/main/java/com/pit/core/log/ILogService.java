package com.pit.core.log;

import com.pit.core.exception.ExceptionUtils;
import com.pit.core.json.GsonUtils;

/**
 * 日志接口
 *
 * @author gy
 * @date 2020-09-12
 */
public interface ILogService {
    /**
     * 对象转字符串，使用 StringBuilder 提高性能，需要预估 StringBuilder 初始化长度
     *
     * @param msg
     * @param objs
     * @return
     */
    static StringBuilder objs2Str(StringBuilder msg, Object[] objs) {
        for (Object obj : objs) {
            if (null == obj) {
                continue;
            }
            msg.append(obj2Str(obj) + "|");
        }
        return msg;
    }

    /**
     * 对象转字符串
     *
     * @param toConvert
     * @return
     */
    static String obj2Str(Object toConvert) {
        if (null == toConvert) {
            return "";
        }

        if (toConvert instanceof Class) {
            return ((Class) toConvert).getName();
        }

        if (toConvert instanceof String) {
            return toConvert.toString();
        }

        if (toConvert instanceof Throwable) {
            return ExceptionUtils.getStackTrace((Throwable) toConvert);
        }

        try {
            return GsonUtils.toJson(toConvert);
        } catch (Exception e) {
            return toConvert.toString();
        }
    }

    /**
     * {@linkplain StringBuilder} 添加日志信息
     *
     * @param builder
     * @param title
     * @param content
     * @param end
     */
    static void append(StringBuilder builder, String title, String content, String end) {
        builder.append(title).append(content).append(end);
    }

    /**
     * 请求开始日志
     *
     * @param obj
     */
    default void sendBeginRequestLog(Object obj) {
        return;
    }

    /**
     * 请求完成日志
     *
     * @param obj
     */
    default void sendEndRequestLog(Object obj) {
        return;
    }

    /**
     * 是否要记日志
     *
     * @return
     */
    default boolean isNeedLog() {
        return false;
    }
}
