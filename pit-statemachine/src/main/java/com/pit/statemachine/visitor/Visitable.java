package com.pit.statemachine.visitor;

/**
 * @Description: 可被访问接口
 * @Author: gy
 * @Date: 2021/9/24
 */
public interface Visitable {
    String accept(final Visitor visitor);
}
