package com.pit.statemachine.impl;

import com.pit.statemachine.StateMachine;
import com.pit.statemachine.component.State;
import com.pit.statemachine.component.Transition;
import com.pit.statemachine.visitor.Visitor;

import java.util.List;
import java.util.Map;

/**
 * @Description: 状态机实现
 * 出于性能方面的考虑，状态机被故意设置为“无状态”，一旦构建完成，它就可以被多线程共享。
 * 但缺点是由于状态机是无状态的，我们无法从状态机中获取当前状态。
 *
 * @Author: gy
 * @Date: 2021/9/24
 */
public class StateMachineImpl<S, E, C> implements StateMachine<S, E, C> {
    private String machineId;

    private final Map<S, State<S, E, C>> stateMap;

    private boolean ready;

    public StateMachineImpl(Map<S, State<S, E, C>> stateMap) {
        this.stateMap = stateMap;
    }

    @Override
    public S fireEvent(S sourceStateId, E event, C ctx) {
        isReady();
        Transition<S, E, C> transition = routeTransition(sourceStateId, event, ctx);

        if (transition == null) {
            Debugger.debug("There is no Transition for " + event);
            return sourceStateId;
        }
        return transition.transit(ctx).getId();
    }

    /**
     * 根据状态与事件选择转换
     * @param sourceStateId
     * @param event
     * @param ctx
     * @return
     */
    private Transition<S, E, C> routeTransition(S sourceStateId, E event, C ctx) {
        State sourceState = getState(sourceStateId);
        /**一个事件可能对应多种转换，找到第一个匹配的转换*/
        List<Transition<S, E, C>> transitions = sourceState.getEventTransitions(event);

        if (transitions == null || transitions.size() == 0) {
            return null;
        }

        Transition<S, E, C> transit = null;
        for (Transition<S, E, C> transition : transitions) {
            if (transition.getCondition() == null) {
                transit = transition;
            } else if (transition.getCondition().isSatisfied(ctx)) {
                transit = transition;
                break;
            }
        }

        return transit;
    }

    @Override
    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    @Override
    public void showStateMachine() {
        SysOutVisitor sysOutVisitor = new SysOutVisitor();
        accept(sysOutVisitor);
    }

    @Override
    public String generatePlantUML() {
        PlantUMLVisitor plantUMLVisitor = new PlantUMLVisitor();
        return accept(plantUMLVisitor);
    }

    @Override
    public String accept(Visitor visitor) {
        StringBuilder sb = new StringBuilder();
        sb.append(visitor.visitOnEntry(this));
        for (State state : stateMap.values()) {
            sb.append(state.accept(visitor));
        }
        sb.append(visitor.visitOnExit(this));
        return sb.toString();
    }

    private void isReady() {
        if (!ready) {
            throw new StateMachineException("State machine is not built yet, can not work");
        }
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    private State getState(S currentStateId) {
        State state = getState(stateMap, currentStateId);
        if (state == null) {
            showStateMachine();
            throw new StateMachineException(currentStateId + " is not found, please check state machine");
        }
        return state;
    }

    private static <S, E, C> State<S, E, C> getState(Map<S, State<S, E, C>> stateMap, S stateId) {
        return stateMap.computeIfAbsent(stateId, k -> new StateImpl<>(stateId));
    }
}
