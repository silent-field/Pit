package com.pit.statemachine.impl;

import com.pit.statemachine.component.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Description: 时间-转换 关系集合
 * @Author: gy
 * @Date: 2021/9/24
 */
public class EventTransitions<S, E, C> {
    /**事件与转换是一对多关系*/
    private HashMap<E, List<Transition<S, E, C>>> eventTransitions = new HashMap<>();

    public void put(E event, Transition<S, E, C> transition) {
        if (eventTransitions.get(event) == null) {
            List<Transition<S, E, C>> transitions = new ArrayList<>();
            transitions.add(transition);
            eventTransitions.put(event, transitions);
        } else {
            List<Transition<S, E, C>> existingTransitions = eventTransitions.get(event);
            verify(existingTransitions, transition);
            existingTransitions.add(transition);
        }
    }

    /**
     * 验证如果已经存在事件-转换关系，不允许重复添加
     * @param existingTransitions
     * @param newTransition
     */
    private void verify(List<Transition<S, E, C>> existingTransitions, Transition<S, E, C> newTransition) {
        for (Transition transition : existingTransitions) {
            if (transition.equals(newTransition)) {
                throw new StateMachineException(transition + " already Exist, you can not add another one");
            }
        }
    }

    public List<Transition<S, E, C>> get(E event) {
        return eventTransitions.get(event);
    }

    public List<Transition<S,E,C>> allTransitions(){
        List<Transition<S,E,C>> allTransitions = new ArrayList<>();
        for(List<Transition<S,E,C>> transitions : eventTransitions.values()){
            allTransitions.addAll(transitions);
        }
        return allTransitions;
    }
}
