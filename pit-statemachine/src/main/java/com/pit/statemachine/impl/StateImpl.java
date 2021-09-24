package com.pit.statemachine.impl;

import com.pit.statemachine.component.State;
import com.pit.statemachine.component.Transition;
import com.pit.statemachine.component.TransitionType;
import com.pit.statemachine.visitor.Visitor;

import java.util.Collection;
import java.util.List;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/24
 */
public class StateImpl<S,E,C> implements State<S,E,C> {
    protected final S stateId;
    private EventTransitions eventTransitions = new EventTransitions();

    public StateImpl(S stateId){
        this.stateId = stateId;
    }

    @Override
    public S getId() {
        return stateId;
    }

    @Override
    public Transition<S, E, C> addTransition(E event, State<S, E, C> target, TransitionType transitionType) {
        Transition<S, E, C> newTransition = new TransitionImpl<>();
        newTransition.setSource(this);
        newTransition.setTarget(target);
        newTransition.setEvent(event);
        newTransition.setType(transitionType);

        Debugger.debug("Begin to add new transition: "+ newTransition);
        eventTransitions.put(event, newTransition);
        return newTransition;
    }

    @Override
    public List<Transition<S, E, C>> getEventTransitions(E event) {
        return eventTransitions.get(event);
    }

    @Override
    public Collection<Transition<S, E, C>> getAllTransitions() {
        return eventTransitions.allTransitions();
    }

    @Override
    public String accept(Visitor visitor) {
        return new StringBuffer(visitor.visitOnEntry(this))
                .append(visitor.visitOnExit(this)).toString();
    }
}
