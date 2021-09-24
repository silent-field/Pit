package com.pit.statemachine.builder;

/**
 * ExternalTransitionsBuilder
 *
 * 多个源状态对应一个目标状态
 */
public interface ExternalTransitionsBuilder<S, E, C> {
    From<S, E, C> fromAmong(S... stateIds);
}
