package com.pit.web.lock;

import com.pit.web.exception.MutexLockException;
import com.pit.web.lock.annotation.ServiceMutexLock;
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
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Aspect
@Component
@Slf4j
public class ServiceMutexLockAspect extends AbstractMutexLockAspect {
    private static final String LOCK_PREFIX_KEY = "serviceMutex";

    @Pointcut("@annotation(com.pit.web.lock.annotation.ServiceMutexLock)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {

        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        ServiceMutexLock serviceMutexLock = method.getAnnotation(ServiceMutexLock.class);
        if (serviceMutexLock == null) {
            return point.proceed();
        }

        String lockKey = createLockKey(serviceMutexLock.parameters(), point);
        if (StringUtils.isBlank(lockKey)) {
            throw new MutexLockException(500, "parameter error");
        }
        return lockHandle(point, serviceMutexLock, lockKey);
    }

    private String createLockKey(String[] parameters, ProceedingJoinPoint point) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                sb.append(".");
            }
            String keyStr = SpELUtils.generateBySpEL(parameters[i], point);
            sb.append(keyStr);
        }
        String parameterKey = sb.toString();

        if (StringUtils.isBlank(parameterKey)) {
            return null;
        }

        String lockKey = String.format("%s:%s", LOCK_PREFIX_KEY, parameterKey);

        return lockKey;
    }

    private Object lockHandle(ProceedingJoinPoint point,
                              ServiceMutexLock mutexLock,
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
}
