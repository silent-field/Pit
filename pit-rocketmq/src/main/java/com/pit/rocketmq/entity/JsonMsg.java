package com.pit.rocketmq.entity;

import lombok.Data;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Data
public abstract class JsonMsg {
    private String traceId;
}
