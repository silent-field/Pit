package com.pit.service.orchestration.constants;

/**
 * @author gy
 */
public interface BaseResultCodeConstants {

    /**
     * 系统成功
     */
    String CODE_SUCCESS = "0";

    /**
     * 系统不设错误代码
     */
    String CODE_NOT_SUPPORT = "--";

    /**
     * 数据已成功
     */
    String CODE_SUCCESS_AND_BREAK = "00";

    /**
     * 数据已失败，但还是继续操作
     */
    String CODE_ERROR_BUT_CONTINUE = "990";
    String CODE_TIME_OUT = "9999";

    /**
     * 非空字段为空
     */
    String CODE_FIELD_NULL_ERROR = "110";

    /**
     * 系统错误
     */
    String CODE_SYSTEM_ERROR = "900";
    /**
     * 服务链处理服务未能成功
     */
    String CODE_900000 = "900000";
    /**
     * 没能选择对应的服务
     */
    String CODE_900001 = "900001";
}
