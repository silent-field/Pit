package com.pit.statemachine.component;

import com.pit.statemachine.visitor.Visitable;

import java.util.Collection;
import java.util.List;

/**
 * @Description: 状态
 *
 * @param <S> 状态类型
 * @param <E> 事件
 * @param <C> 上下文
 *
 * @Author: gy
 * @Date: 2021/9/24
 */
public interface State<S, E, C> extends Visitable {
    /**
     * 状态标识
     * @return
     */
    S getId();

    /**
     * 添加状态变换
     * @param event 变换对应的事件
     * @param target    变换的目标状态
     * @param transitionType    变换的类型
     * @return
     */
    Transition<S, E, C> addTransition(E event, State<S, E, C> target, TransitionType transitionType);

    /**
     * 事件对应的变换集合
     * @param event
     * @return
     */
    List<Transition<S, E, C>> getEventTransitions(E event);

    /**
     * 所有的变换
     * @return
     */
    Collection<Transition<S, E, C>> getAllTransitions();

}
