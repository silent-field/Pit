package com.pit.core.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author gy
 * @date 2020-09-12
 */
public class ExceptionUtils {

    public static String getStackTrace(Throwable t) {
        if (null == t) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}