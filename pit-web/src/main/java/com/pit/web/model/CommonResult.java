package com.pit.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pit.core.json.GsonUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 标准响应结果定义
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
public class CommonResult<T> {

    @Setter
    private long defaultSuccessCode = 0;

    /** 业务状态码,为了和http状态码区分,不建议使用1xx~5xx */
    @Setter
    @Getter
    private long code;

    /** 请求数据,仅业务状态码正常情况下有效 */
    @Setter
    @Getter
    private T data;

    /** 错误描述信息,仅业务状态码非正常下有效 */
    @Setter
    @Getter
    private String message;

    public CommonResult() {
        this.code = defaultSuccessCode;
    }

    public CommonResult(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public CommonResult(T data) {
        this.code = defaultSuccessCode;
        this.data = data;
        this.message = "ok";
    }

    public CommonResult(long code, T data) {
        this.code = code;
        this.data = data;
    }

    public CommonResult(long code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return defaultSuccessCode == code;
    }
}
