package com.pit.statemachine.impl;

import com.pit.statemachine.StateMachine;
import com.pit.statemachine.component.State;
import com.pit.statemachine.component.Transition;
import com.pit.statemachine.visitor.Visitor;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/24
 */
public class SysOutVisitor implements Visitor {
    @Override
    public String visitOnEntry(StateMachine<?, ?, ?> stateMachine) {
        String entry = "-----StateMachine:" + stateMachine.getMachineId() + "-------";
        System.out.println(entry);
        return entry;
    }

    @Override
    public String visitOnExit(StateMachine<?, ?, ?> stateMachine) {
        String exit = "------------------------";
        System.out.println(exit);
        return exit;
    }

    @Override
    public String visitOnEntry(State<?, ?, ?> state) {
        StringBuilder sb = new StringBuilder();
        String stateStr = "State:" + state.getId();
        sb.append(stateStr).append(LF);
        System.out.println(stateStr);
        for (Transition transition : state.getAllTransitions()) {
            String transitionStr = "    Transition:" + transition;
            sb.append(transitionStr).append(LF);
            System.out.println(transitionStr);
        }
        return sb.toString();
    }

    @Override
    public String visitOnExit(State<?, ?, ?> state) {
        return StringUtils.EMPTY;
    }
}
