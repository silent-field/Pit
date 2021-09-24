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
public class PlantUMLVisitor  implements Visitor {
    @Override
    public String visitOnEntry(StateMachine<?, ?, ?> stateMachine) {
        return "@startuml" + LF;
    }

    @Override
    public String visitOnExit(StateMachine<?, ?, ?> stateMachine) {
        return "@enduml";
    }

    @Override
    public String visitOnEntry(State<?, ?, ?> state) {
        StringBuilder sb = new StringBuilder();
        for(Transition transition: state.getAllTransitions()){
            sb.append(transition.getSource().getId())
                    .append(" --> ")
                    .append(transition.getTarget().getId())
                    .append(" : ")
                    .append(transition.getEvent())
                    .append(LF);
        }
        return sb.toString();
    }

    @Override
    public String visitOnExit(State<?, ?, ?> state) {
        return StringUtils.EMPTY;
    }
}
