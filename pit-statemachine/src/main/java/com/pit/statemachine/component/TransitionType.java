package com.pit.statemachine.component;

/**
 * @Description: 转换类型
 * @Author: gy
 * @Date: 2021/9/24
 */
public enum TransitionType {
    /**
     * 这种转换类型如果被触发，在不退出或进入源状态的情况下发生（即，它不会导致状态改变）。
     * 这意味着不会调用源 State 的进入或退出条件。 即使 SateMachine 位于嵌套在关联状态内的一个或多个区域中，也可以进行内部转换。
     */
    INTERNAL,

    /**
     * 这种转换类型如果被触发，将不会变换状态，但它将退出并重新进入当前状态。
     */
    LOCAL,

    /**
     * 这种转换类型如果被触发，将退出源状态
     */
    EXTERNAL
}
