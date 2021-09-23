package com.pit.web.lock;

import com.pit.web.exception.MutexLockException;
import com.pit.web.lock.annotation.MutexLock;
import com.pit.web.util.SpELUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Aspect
@Component
@Slf4j
public class MutexLockAspect extends AbstractMutexLockAspect {
    private static final String LOCK_PREFIX_KEY = "mutex";

    @Pointcut("@annotation(com.pit.web.lock.annotation.MutexLock)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {

        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        MutexLock mutexLock = method.getAnnotation(MutexLock.class);
        if (mutexLock == null) {
            return point.proceed();
        }

        String lockKey = createLockKey(mutexLock.parameters(), point.getArgs(), method);
        if (StringUtils.isBlank(lockKey)) {
            throw new MutexLockException(500, "parameter error");
        }
        return lockHandle(point, mutexLock, lockKey);
    }

    private Object lockHandle(ProceedingJoinPoint point,
                              MutexLock mutexLock,
                              String lockKey) throws Throwable {

        RedissonClient redissonClient = getRedissonClient();
        if (redissonClient == null) {
            throw new IllegalArgumentException("RedissonClient error");
        }

        RLock rLock = redissonClient.getLock(lockKey);
        try {
            boolean lockSuccess = rLock.tryLock(-1, mutexLock.leaseTime(), TimeUnit.SECONDS);
            if (lockSuccess) {
                return point.proceed();
            } else {
                return failLockResponse();
            }
        } finally {
            if (rLock.isHeldByCurrentThread() && mutexLock.autoUnlock()) {
                rLock.unlock();
            }
        }
    }

    private String createLockKey(String[] parameters, Object[] args, Method method) {

        String parameterKey = Arrays.stream(parameters)
                .map(parameter -> {

                    String value = SpELUtils.parse(parameter, args, method);
                    if (StringUtils.isBlank(value)) {
                        return "";
                    }
                    return String.format("%s=%s", parameter.substring(1), value);
                })
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(";"));

        if (StringUtils.isBlank(parameterKey)) {
            return null;
        }

        String businessKey = String.format(
                "%s.%s:%s",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                parameterKey
        );

        String lockKey = String.format("%s:%s:%s", getAppName(), LOCK_PREFIX_KEY, businessKey);

        return lockKey;
    }
}
