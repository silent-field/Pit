package com.pit.statemachine.visitor;

import com.pit.statemachine.StateMachine;
import com.pit.statemachine.component.State;

/**
 * @Description: 访问者模式，用于访问状态机元素
 * @Author: gy
 * @Date: 2021/9/24
 */
public interface Visitor {
    /**
     * 换行符
     */
    char LF = '\n';

    /**
     * @param visitable 被访问的元素
     * @return
     */
    String visitOnEntry(StateMachine<?, ?, ?> visitable);

    /**
     * @param visitable 被访问的元素
     * @return
     */
    String visitOnExit(StateMachine<?, ?, ?> visitable);

    /**
     * @param visitable 被访问的元素
     * @return
     */
    String visitOnEntry(State<?, ?, ?> visitable);

    /**
     * @param visitable 被访问的元素
     * @return
     */
    String visitOnExit(State<?, ?, ?> visitable);
}
