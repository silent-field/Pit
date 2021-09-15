package com.pit.web.log;

import org.slf4j.MDC;


/**
 * MDC工具类
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
public class MDCUtil {

    public final static String TRACE_ID_LOGIC_TOKEN = "token_key";
    public final static String TRACE_ID_REMOTE_IP = "remote_ip";
    public final static String TRACE_ID_UID = "uid";

    /** 客户端IP */
    public final static String CLIENT_IP = "client_ip";
    /** 单次请求唯一ID */
    public final static String REQ_ID = "req_id";
    /** 链路追踪ID */
    public final static String TRACE_ID = "trace_id";

    public final static String URI = "uri";

    public static final String METHOD = "method";

    public static void put(String key, String value) {
        MDC.put(key, value);
    }
}
