package com.pit.statemachine.component;

/**
 * @Description: 状态机概念-动作，事件发生以后要执行动作。Action一般对应一个函数。
 * @Author: gy
 * @Date: 2021/9/24
 */
public interface Action<S, E, C> {
    void execute(S from, S to, E event, C context);
}
