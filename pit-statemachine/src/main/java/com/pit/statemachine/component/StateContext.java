package com.pit.statemachine.component;

import com.pit.statemachine.StateMachine;

/**
 * @Description: 状态上下文
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型(用户自定义的上下文)
 *
 * @Author: gy
 * @Date: 2021/9/24
 */
public interface StateContext<S, E, C> {
    /**
     * 获取转换
     * @return
     */
    Transition<S, E, C> getTransition();

    /**
     * 获取状态机
     * @return
     */
    StateMachine<S, E, C> getStateMachine();
}
