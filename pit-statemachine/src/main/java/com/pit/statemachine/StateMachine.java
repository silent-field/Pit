package com.pit.statemachine;

import com.pit.statemachine.visitor.Visitable;

/**
 * @Description: 状态机
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型(用户自定义的上下文)
 *
 * @Author: gy
 * @Date: 2021/9/24
 */
public interface StateMachine<S, E, C> extends Visitable {
    /**
     * 触发事件 {@code E}
     *
     * @param sourceStateId 源状态标识
     * @param event 事件
     * @param ctx   上下文
     * @return
     */
    S fireEvent(S sourceStateId, E event, C ctx);

    /**
     * 状态机标识
     *
     * @return
     */
    String getMachineId();

    /**
     * 通过访问者模式打印状态机结构
     */
    void showStateMachine();

    /**
     * 通过访问者模式打印状态机结构(uml)
     */
    String generatePlantUML();

}
