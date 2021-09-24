package com.pit.statemachine.builder;

import com.pit.statemachine.StateMachine;

public interface StateMachineBuilder<S, E, C> {
    /**
     * 返回 ExternalTransitionBuilder
     * @return External transition builder
     */
    ExternalTransitionBuilder<S, E, C> externalTransition();

    /**
     * 返回 ExternalTransitionsBuilder
     * @return External transition builder
     */
    ExternalTransitionsBuilder<S, E, C> externalTransitions();

    /**
     * 返回 InternalTransitionBuilder
     * @return Internal transition builder
     */
    InternalTransitionBuilder<S, E, C> internalTransition();

    StateMachine<S, E, C> build(String machineId);

}
