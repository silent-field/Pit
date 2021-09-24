package com.pit.statemachine.builder;

/**
 * 用于创建一个转换
 * @param <S>
 * @param <E>
 * @param <C>
 */
public interface ExternalTransitionBuilder<S, E, C> {
    /**
     * Build transition source state.
     * @param stateId id of state
     * @return from clause builder
     */
    From<S, E, C> from(S stateId);
}