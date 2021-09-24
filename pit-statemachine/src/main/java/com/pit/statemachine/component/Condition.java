package com.pit.statemachine.component;

/**
 * @Description: 用于判断上下文是否满足特定条件
 * @Author: gy
 * @Date: 2021/9/24
 */
public interface Condition<C> {
    /**
     * @param context 上下文
     * @return 上下文是否满足当前条件
     */
    boolean isSatisfied(C context);

    default String name(){
        return this.getClass().getSimpleName();
    }
}
