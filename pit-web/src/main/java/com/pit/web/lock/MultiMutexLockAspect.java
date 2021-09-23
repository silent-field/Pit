package com.pit.web.lock;

import com.pit.web.exception.MutexLockException;
import com.pit.web.lock.annotation.MultiMutexLock;
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
import java.util.*;
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
public class MultiMutexLockAspect extends AbstractMutexLockAspect {
    private static final String LOCK_PREFIX_KEY = "multiMutex";

    @Pointcut("@annotation(com.pit.web.lock.annotation.MultiMutexLock)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {

        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        MultiMutexLock multiMutexLock = method.getAnnotation(MultiMutexLock.class);
        if (multiMutexLock == null) {
            return point.proceed();
        }

        List<String> multiLockKeys = createMultiLockKeys(multiMutexLock.multiKeys(), point.getArgs(), method);

        log.info("mutexLockKey:{}", multiLockKeys);
        return multiLockHandle(point, multiMutexLock, multiLockKeys);
    }

    private Object multiLockHandle(ProceedingJoinPoint point,
                                   MultiMutexLock MultiMutexLock,
                                   List<String> multiLockKeys) throws Throwable {

        RedissonClient redissonClient = getRedissonClient();
        if (redissonClient == null) {
            throw new IllegalArgumentException("RedissonClient error");
        }

        // 记录已上锁集合
        Set<RLock> lockedSet = new HashSet<>();
        try {
            // 锁排序和去重
            Set<String> multiLockKeySet = new TreeSet<>(multiLockKeys);
            for (String lockKey : multiLockKeySet) {
                RLock rLock = redissonClient.getLock(lockKey);
                boolean lockSuccess = rLock.tryLock(-1, MultiMutexLock.leaseTime(), TimeUnit.SECONDS);
                if (lockSuccess) {
                    lockedSet.add(rLock);
                } else {
                    // 一个失败 释放全部获取到的锁
                    for (RLock rl : lockedSet) {
                        log.error("获取锁失败，释放全部锁：{}", rl.getName());
                        rl.unlock();
                    }
                    return failLockResponse();
                }
            }
            return point.proceed();
        } finally {
            for (RLock rl : lockedSet) {
                if (rl.isHeldByCurrentThread() && MultiMutexLock.autoUnlock()) {
                    rl.unlock();
                }
            }
        }
    }

    private List<String> createMultiLockKeys(String multiKeys, Object[] args, Method method) {

        if (StringUtils.isEmpty(multiKeys)) {
            throw new MutexLockException(500, "multiKeys error");
        }

        String businessKey = String.format(
                "%s.%s",
                method.getDeclaringClass().getSimpleName(),
                method.getName()
        );
        String lockKeyPrefix = String.format("%s:%s:%s", getAppName(), LOCK_PREFIX_KEY, businessKey);

        String paramValueString = SpELUtils.parse(multiKeys, args, method);
        if (StringUtils.isEmpty(paramValueString)) {
            throw new MutexLockException(500, "multiKeys value error");
        }
        String[] paramValues = paramValueString.split(",");
        return Arrays.asList(paramValues).stream().map((key ->
                String.format("%s.%s", lockKeyPrefix, key)
        )).collect(Collectors.toList());
    }
}
