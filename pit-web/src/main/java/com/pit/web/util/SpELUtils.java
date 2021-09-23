package com.pit.web.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class SpELUtils {
    /**
     * 用于SpEL表达式解析.
     */
    private static SpelExpressionParser parser = new SpelExpressionParser();
    /**
     * 用于获取方法参数定义名字.
     */
    private static DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public static String generateBySpEL(String spELString, ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());
            EvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            Expression expression = parser.parseExpression(spELString);
            return expression.getValue(context, String.class);
        } catch (Exception e) {
            return spELString;
        }
    }

    public static String parse(String el, Object[] args, Method method) {

        String[] paramNames = nameDiscoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        Expression expression = parser.parseExpression(el);
        return expression.getValue(context, String.class);
    }
}