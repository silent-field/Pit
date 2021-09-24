package com.pit.statemachine.component;

/**
 * @Description:  {@code Transition} 用于表示状态机状态间如何进行转换，画成图表现为边
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型(用户自定义的上下文)
 *
 * @Author: gy
 * @Date: 2021/9/24
 */
public interface Transition<S, E, C> {
    /**
     * 获取源状态
     *
     * @return
     */
    State<S, E, C> getSource();

    void setSource(State<S, E, C> state);

    /**
     * 获取事件
     *
     * @return
     */
    E getEvent();

    void setEvent(E event);

    /**
     * 设置变化类型
     *
     * @param type
     */
    void setType(TransitionType type);

    /**
     * 获取目标状态
     *
     * @return
     */
    State<S, E, C> getTarget();

    void setTarget(State<S, E, C> state);

    /**
     * 获取条件
     *
     * @return
     */
    Condition<C> getCondition();

    void setCondition(Condition<C> condition);

    /**
     * 转换状态时触发动作
     *
     * @return
     */
    Action<S,E,C> getAction();

    void setAction(Action<S, E, C> action);

    /**
     * 执行转换，从源状态变为目标状态
     *
     * @param ctx
     * @return
     */
    State<S,E,C> transit(C ctx);

    /**
     * 验证转换正确性
     */
    void verify();
}
