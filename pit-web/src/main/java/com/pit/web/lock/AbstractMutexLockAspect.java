package com.pit.web.lock;

import com.pit.web.exception.MutexLockException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Slf4j
public abstract class AbstractMutexLockAspect implements EnvironmentAware {
    @Getter
    protected Environment environment;

    @Autowired
    protected RedissonClient redissonClient;

    protected RedissonClient getRedissonClient() {
        return redissonClient;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected String getAppName() {
        return environment.getProperty("spring.application.name");
    }

    protected Object failLockResponse() {
        throw new MutexLockException(500, "lock fail");
    }
}
